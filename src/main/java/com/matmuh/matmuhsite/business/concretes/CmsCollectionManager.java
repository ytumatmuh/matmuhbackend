package com.matmuh.matmuhsite.business.concretes;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import com.matmuh.matmuhsite.business.abstracts.CmsCollectionProvider;
import com.matmuh.matmuhsite.business.abstracts.CmsCollectionService;
import com.matmuh.matmuhsite.business.constants.CmsMessages;
import com.matmuh.matmuhsite.business.constants.CollectionRegistry;
import com.matmuh.matmuhsite.core.helpers.CollectionFilterParser;
import com.matmuh.matmuhsite.core.helpers.CollectionSchemaValidator;
import com.matmuh.matmuhsite.core.helpers.SlugGenerator;
import com.matmuh.matmuhsite.core.helpers.SlugNormalizer;
import com.matmuh.matmuhsite.core.dtos.cms.request.CreateCollectionItemRequestDto;
import com.matmuh.matmuhsite.core.dtos.cms.request.SaveDraftRequestDto;
import com.matmuh.matmuhsite.core.dtos.cms.request.SaveNewDraftRequestDto;
import com.matmuh.matmuhsite.core.dtos.cms.request.UpsertCollectionItemRequestDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionItemDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionListDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.MyCollectionDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionSchema;
import com.matmuh.matmuhsite.entities.cms.SlugSource;
import com.matmuh.matmuhsite.core.exceptions.ResourceNotFoundException;
import com.matmuh.matmuhsite.core.exceptions.CmsValidationException;
import com.matmuh.matmuhsite.core.exceptions.ConcurrencyConflictException;
import com.matmuh.matmuhsite.dataAccess.abstracts.cms.CollectionDraftDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.cms.CollectionItemDao;
import com.matmuh.matmuhsite.entities.cms.CollectionDraft;
import com.matmuh.matmuhsite.entities.cms.CollectionItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CmsCollectionManager implements CmsCollectionService {

    private static final UUID EMPTY_UUID = new UUID(0, 0);

    private static final JsonNodeFactory NODES = JsonNodeFactory.instance;

    private final Logger logger = LoggerFactory.getLogger(CmsCollectionManager.class);

    private final CollectionItemDao collectionItemDao;
    private final CollectionDraftDao collectionDraftDao;
    private final CollectionRegistry registry;
    private final Map<String, CmsCollectionProvider> providers;

    public CmsCollectionManager(CollectionItemDao collectionItemDao,
                                CollectionDraftDao collectionDraftDao,
                                CollectionRegistry registry,
                                List<CmsCollectionProvider> providers) {
        this.collectionItemDao = collectionItemDao;
        this.collectionDraftDao = collectionDraftDao;
        this.registry = registry;
        this.providers = providers.stream()
                .collect(Collectors.toMap(CmsCollectionProvider::collectionKey, provider -> provider));
    }


    @Override
    public boolean allowsAnonymousRead(String collectionKey) {
        return registry.exists(collectionKey)
                && registry.resolve(collectionKey).allowAnonymousRead();
    }

    @Override
    public CollectionSchema getSchema(String collectionKey) {
        return registry.resolve(collectionKey).schema();
    }

    @Override
    public List<MyCollectionDto> getMyCollections() {
        return registry.all().stream()
                .map(def -> new MyCollectionDto(def.key(), def.schema(), true, def.slugSource()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionListDto list(String collectionKey, String userId,
                                  Map<String, String> filters, int offset, int limit) {
        var def = registry.resolve(collectionKey);
        var key = def.key();

        logger.info("Listing collection {} filters={} offset={} limit={} editor={}",
                key, filters == null ? Set.of() : filters.keySet(), offset, limit, userId != null);

        var filterNode = CollectionFilterParser.build(def.schema(), filters);
        var filterJson = filterNode == null ? null : filterNode.toString();

        var provider = providers.get(key);

        long total;
        List<CollectionItemDto> itemDtos;

        if (provider != null) {
            var result = provider.list(filterNode, offset, limit);
            total = result.getTotal();
            itemDtos = new ArrayList<>(result.getItems());
        } else {
            var items = collectionItemDao.searchByFilter(key, filterJson, offset, limit);
            total = collectionItemDao.countByFilter(key, filterJson);
            itemDtos = items.stream().map(this::toDto).collect(Collectors.toCollection(ArrayList::new));
        }

        if (userId != null) {
            for (var dto : itemDtos) {
                dto.setCanEdit(true);
                dto.setDraftData(resolveItemDraft(key, dto.getSlug(), userId, dto.getData()));
            }
            if (filterJson == null && offset == 0) {
                appendNewItemDraftRow(key, userId, itemDtos);
            }
        }

        return new CollectionListDto(itemDtos, total, offset, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionItemDto getBySlug(String collectionKey, String slug, String userId) {
        var def = registry.resolve(collectionKey);
        var key = def.key();
        var normalizedSlug = SlugNormalizer.normalizeBlockPath(slug);

        var provider = providers.get(key);
        var dto = provider != null
                ? provider.getBySlug(normalizedSlug)
                : toDto(collectionItemDao.findByCollectionKeyAndSlugAndArchivedFalse(key, normalizedSlug)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                CmsMessages.COLLECTION_ITEM_NOT_FOUND + key + "/" + normalizedSlug)));

        if (userId != null) {
            dto.setCanEdit(true);
            dto.setDraftData(resolveItemDraft(key, normalizedSlug, userId, dto.getData()));
        }
        return dto;
    }


    @Override
    @Transactional
    public CollectionItemDto upsert(String collectionKey, String slug,
                                    UpsertCollectionItemRequestDto request, String updatedBy) {
        var def = registry.resolve(collectionKey);
        var key = def.key();
        var normalizedSlug = SlugNormalizer.normalizeBlockPath(slug);

        logger.info("Upserting collection item {}/{} by {}", key, normalizedSlug, updatedBy);

        var validated = CollectionSchemaValidator.validateAndStrip(def.schema(), request.getData());

        var provider = providers.get(key);
        if (provider != null) {
            if (!provider.existsBySlug(normalizedSlug) && def.slugSource() == SlugSource.AUTO_GENERATED) {
                throw new CmsValidationException(CmsMessages.AUTO_GENERATED_USE_POST);
            }
            var providedDto = provider.upsert(normalizedSlug, validated, request.getVersion());
            deleteDrafts(key, normalizedSlug, updatedBy, false);
            providedDto.setCanEdit(true);
            return providedDto;
        }

        var item = collectionItemDao.findByCollectionKeyAndSlug(key, normalizedSlug).orElse(null);
        var isCreate = item == null;

        if (isCreate) {
            if (def.slugSource() == SlugSource.AUTO_GENERATED) {
                throw new CmsValidationException(CmsMessages.AUTO_GENERATED_USE_POST);
            }
            item = CollectionItem.builder()
                    .collectionKey(key)
                    .slug(normalizedSlug)
                    .data(validated)
                    .updatedBy(updatedBy)
                    .build();
        } else {
            if (request.getVersion() != null && request.getVersion() != item.getVersion()) {
                throw new ConcurrencyConflictException(CmsMessages.VERSION_CONFLICT);
            }
            if (item.isArchived()) {
                item.setArchived(false);
                item.setArchivedAt(null);
            }
            item.setData(validated);
            touch(item, updatedBy);
        }

        var saved = collectionItemDao.save(item);

        collectionDraftDao.findByCollectionKeyAndSlugAndUserIdAndForNewItemFalse(key, normalizedSlug, updatedBy)
                .ifPresent(collectionDraftDao::delete);
        if (isCreate) {
            collectionDraftDao.findByCollectionKeyAndUserIdAndForNewItemTrue(key, updatedBy)
                    .ifPresent(collectionDraftDao::delete);
        }

        var dto = toDto(saved);
        dto.setCanEdit(true);
        return dto;
    }

    @Override
    @Transactional
    public CollectionItemDto createWithAutoSlug(String collectionKey,
                                                CreateCollectionItemRequestDto request, String updatedBy) {
        var def = registry.resolve(collectionKey);
        var key = def.key();

        if (def.slugSource() == SlugSource.USER_DEFINED) {
            throw new CmsValidationException(CmsMessages.USER_DEFINED_USE_PUT);
        }

        var validated = CollectionSchemaValidator.validateAndStrip(def.schema(), request.getData());

        var source = validated.path(def.slugSourceField()).asText("");
        var base = SlugGenerator.slugify(source);
        if (base.isBlank()) {
            throw new CmsValidationException(CmsMessages.SLUG_SOURCE_FIELD_MISSING);
        }
        var provider = providers.get(key);
        if (provider != null) {
            var providedDto = provider.create(validated);
            deleteDrafts(key, null, updatedBy, true);
            providedDto.setCanEdit(true);
            logger.info("Created collection item {}/{} by {}", key, providedDto.getSlug(), updatedBy);
            return providedDto;
        }

        var slug = resolveUniqueSlug(key, base);

        logger.info("Creating collection item {}/{} by {}", key, slug, updatedBy);

        var item = CollectionItem.builder()
                .collectionKey(key)
                .slug(slug)
                .data(validated)
                .updatedBy(updatedBy)
                .build();

        var saved = collectionItemDao.save(item);

        collectionDraftDao.findByCollectionKeyAndUserIdAndForNewItemTrue(key, updatedBy)
                .ifPresent(collectionDraftDao::delete);

        var dto = toDto(saved);
        dto.setCanEdit(true);
        return dto;
    }

    @Override
    @Transactional
    public void saveItemDraft(String collectionKey, String slug, String userId, SaveDraftRequestDto request) {
        var def = registry.resolve(collectionKey);
        var key = def.key();
        var normalizedSlug = SlugNormalizer.normalizeBlockPath(slug);

        var validated = CollectionSchemaValidator.validateAndStrip(def.schema(), request.getData(), true);

        var draft = collectionDraftDao
                .findByCollectionKeyAndSlugAndUserIdAndForNewItemFalse(key, normalizedSlug, userId)
                .orElseGet(() -> CollectionDraft.builder()
                        .collectionKey(key)
                        .slug(normalizedSlug)
                        .userId(userId)
                        .build());

        draft.setPayload(validated);
        collectionDraftDao.save(draft);
    }

    @Override
    @Transactional
    public void saveNewDraft(String collectionKey, String userId, SaveNewDraftRequestDto request) {
        var def = registry.resolve(collectionKey);
        var key = def.key();

        var validated = CollectionSchemaValidator.validateAndStrip(def.schema(), request.getData(), true);

        var slug = CollectionDraft.DEFAULT_SLUG;
        if (def.slugSource() == SlugSource.USER_DEFINED) {
            if (request.getSlug() == null || request.getSlug().isBlank()) {
                throw new CmsValidationException(CmsMessages.SLUG_REQUIRED_FOR_NEW_DRAFT);
            }
            slug = SlugNormalizer.normalizeBlockPath(request.getSlug());
            var provider = providers.get(key);
            var taken = provider != null
                    ? provider.existsBySlug(slug)
                    : collectionItemDao.existsByCollectionKeyAndSlug(key, slug);
            if (taken) {
                throw new CmsValidationException(CmsMessages.SLUG_ALREADY_IN_USE + slug);
            }
        }

        var draft = collectionDraftDao
                .findByCollectionKeyAndUserIdAndForNewItemTrue(key, userId)
                .orElseGet(() -> CollectionDraft.builder()
                        .collectionKey(key)
                        .userId(userId)
                        .forNewItem(true)
                        .build());

        draft.setSlug(slug);
        draft.setPayload(validated);
        collectionDraftDao.save(draft);
    }

    @Override
    @Transactional
    public void deleteItemDraft(String collectionKey, String slug, String userId) {
        var key = registry.resolve(collectionKey).key();
        var normalizedSlug = SlugNormalizer.normalizeBlockPath(slug);

        collectionDraftDao
                .findByCollectionKeyAndSlugAndUserIdAndForNewItemFalse(key, normalizedSlug, userId)
                .ifPresent(collectionDraftDao::delete);
    }

    @Override
    @Transactional
    public void deleteNewDraft(String collectionKey, String userId) {
        var key = registry.resolve(collectionKey).key();

        collectionDraftDao
                .findByCollectionKeyAndUserIdAndForNewItemTrue(key, userId)
                .ifPresent(collectionDraftDao::delete);
    }



    private void deleteDrafts(String collectionKey, String slug, String userId, boolean isCreate) {
        if (slug != null) {
            collectionDraftDao.findByCollectionKeyAndSlugAndUserIdAndForNewItemFalse(collectionKey, slug, userId)
                    .ifPresent(collectionDraftDao::delete);
        }
        if (isCreate) {
            collectionDraftDao.findByCollectionKeyAndUserIdAndForNewItemTrue(collectionKey, userId)
                    .ifPresent(collectionDraftDao::delete);
        }
    }

    private JsonNode resolveItemDraft(String collectionKey, String slug, String userId, JsonNode publishedData) {
        return collectionDraftDao
                .findByCollectionKeyAndSlugAndUserIdAndForNewItemFalse(collectionKey, slug, userId)
                .map(CollectionDraft::getPayload)
                .filter(payload -> !payload.equals(publishedData))
                .orElse(null);
    }

    private void appendNewItemDraftRow(String collectionKey, String userId, List<CollectionItemDto> itemDtos) {
        collectionDraftDao.findByCollectionKeyAndUserIdAndForNewItemTrue(collectionKey, userId)
                .filter(draft -> !isEffectivelyEmpty(draft.getPayload()))
                .ifPresent(draft -> {
                    var row = new CollectionItemDto(
                            EMPTY_UUID, collectionKey, draft.getSlug(),
                            NODES.objectNode(), 0, true, draft.getPayload());
                    itemDtos.add(row);
                });
    }

    private boolean isEffectivelyEmpty(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) return true;
        if (node.isTextual()) return node.asText().isBlank();
        if (node.isBoolean()) return !node.asBoolean();
        if (node.isNumber()) return node.decimalValue().signum() == 0;
        if (node.isArray()) {
            for (JsonNode item : node) {
                if (!isEffectivelyEmpty(item)) return false;
            }
            return true;
        }
        if (node.isObject()) {
            for (JsonNode value : node) {
                if (!isEffectivelyEmpty(value)) return false;
            }
            return true;
        }
        return false;
    }

    private String resolveUniqueSlug(String collectionKey, String base) {
        var candidate = base;
        int suffix = 2;
        while (collectionItemDao.existsByCollectionKeyAndSlug(collectionKey, candidate)) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }

    private void touch(CollectionItem item, String updatedBy) {
        item.setUpdatedBy(updatedBy);
        item.setVersion(item.getVersion() + 1);
    }

    private CollectionItemDto toDto(CollectionItem item) {
        return new CollectionItemDto(
                item.getId(),
                item.getCollectionKey(),
                item.getSlug(),
                item.getData(),
                item.getVersion(),
                false,
                null);
    }
}
