package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.ContentService;
import com.matmuh.matmuhsite.business.constants.CmsMessages;
import com.matmuh.matmuhsite.core.dtos.cms.request.SyncManifestRequestDto;
import com.matmuh.matmuhsite.core.dtos.cms.request.UpdatePageRequestDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.BlockDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.ContentResponseDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.SyncResultDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.UpdatePageResponseDto;
import com.matmuh.matmuhsite.core.exceptions.ConcurrencyConflictException;
import com.matmuh.matmuhsite.core.helpers.CmsLocaleResolver;
import com.matmuh.matmuhsite.core.helpers.SlugNormalizer;
import com.matmuh.matmuhsite.dataAccess.abstracts.cms.ContentBlockDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.cms.ContentDraftDao;
import com.matmuh.matmuhsite.entities.cms.ContentBlock;
import com.matmuh.matmuhsite.entities.cms.ContentDraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ContentManager implements ContentService {

    private final Logger logger = LoggerFactory.getLogger(ContentManager.class);

    private final ContentBlockDao contentBlockDao;
    private final ContentDraftDao contentDraftDao;
    private final ObjectMapper objectMapper;
    private final CmsLocaleResolver localeResolver;

    public ContentManager(ContentBlockDao contentBlockDao,
                          ContentDraftDao contentDraftDao,
                          ObjectMapper objectMapper,
                          CmsLocaleResolver localeResolver) {
        this.contentBlockDao = contentBlockDao;
        this.contentDraftDao = contentDraftDao;
        this.objectMapper = objectMapper;
        this.localeResolver = localeResolver;
    }


    @Override
    @Transactional(readOnly = true)
    public ContentResponseDto getPublishedBySlug(String slug, String locale) {
        var normalizedSlug = SlugNormalizer.normalizeSlug(slug);
        var resolved = localeResolver.resolveForRead(locale);
        logger.info("Fetching published content for slug: {} locale: {}", normalizedSlug, resolved);

        var blocks = contentBlockDao.findPublished(normalizedSlug, resolved);

        return new ContentResponseDto(normalizedSlug, resolved, toBlockDtos(blocks));
    }

    @Override
    @Transactional(readOnly = true)
    public ContentResponseDto getBySlugForEditor(String userId, String slug, String locale) {
        var normalizedSlug = SlugNormalizer.normalizeSlug(slug);
        var resolved = localeResolver.resolveForRead(locale);
        logger.info("Fetching editor content for slug: {} locale: {} user: {}", normalizedSlug, resolved, userId);

        var blocks = contentBlockDao.findPublished(normalizedSlug, resolved);
        var blockDtos = toBlockDtos(blocks);

        contentDraftDao.findOwn(normalizedSlug, userId, resolved).ifPresent(draft -> {
            var draftValues = parseDraftPayload(draft.getPayload());
            for (BlockDto dto : blockDtos) {
                var draftValue = draftValues.get(dto.getBlockPath());
                if (draftValue != null && !draftValue.equals(dto.getValue())) {
                    dto.setDraftValue(draftValue);
                }
            }
            logger.info("Resolved draft with {} block(s) for slug: {}", draftValues.size(), normalizedSlug);
        });

        return new ContentResponseDto(normalizedSlug, resolved, blockDtos);
    }


    @Override
    @Transactional
    public UpdatePageResponseDto updatePage(String userId, UpdatePageRequestDto request, String locale) {
        var normalizedSlug = SlugNormalizer.normalizeSlug(request.getSlug());
        var resolved = localeResolver.resolveForWrite(locale);
        logger.info("Publishing {} block(s) for slug: {} by user: {}",
                request.getBlocks().size(), normalizedSlug, userId);

        var paths = request.getBlocks().stream()
                .map(update -> SlugNormalizer.normalizeBlockPath(update.getBlockPath()))
                .toList();

        var blocksByPath = contentBlockDao
                .findForUpdate(normalizedSlug, paths, resolved)
                .stream()
                .collect(Collectors.toMap(ContentBlock::getBlockPath, Function.identity()));

        int updated = 0;
        int unchanged = 0;
        var toSave = new ArrayList<ContentBlock>();

        for (var update : request.getBlocks()) {
            var blockPath = SlugNormalizer.normalizeBlockPath(update.getBlockPath());
            var block = blocksByPath.get(blockPath);
            if (block == null) {
                logger.warn("Ignoring unknown blockPath on publish: {}.{}", normalizedSlug, blockPath);
                continue;
            }

            if (update.getVersion() != null && update.getVersion() != block.getVersion()) {
                throw new ConcurrencyConflictException(CmsMessages.VERSION_CONFLICT);
            }

            if (Objects.equals(block.getValue(), update.getValue())) {
                unchanged++;
                continue;
            }

            block.setValue(update.getValue());
            touch(block, userId);
            toSave.add(block);
            updated++;
        }

        contentBlockDao.saveAll(toSave);
        contentDraftDao.deleteOwn(normalizedSlug, userId, resolved);

        return new UpdatePageResponseDto(updated, unchanged);
    }

    @Override
    @Transactional
    public void saveDraft(String userId, UpdatePageRequestDto request, String locale) {
        var normalizedSlug = SlugNormalizer.normalizeSlug(request.getSlug());
        var resolved = localeResolver.resolveForWrite(locale);
        logger.info("Saving draft for slug: {} locale: {} user: {}", normalizedSlug, resolved, userId);

        JsonNode payload = objectMapper.valueToTree(request.getBlocks());

        var draft = contentDraftDao.findOwn(normalizedSlug, userId, resolved)
                .orElseGet(() -> ContentDraft.builder()
                        .slug(normalizedSlug)
                        .userId(userId)
                        .locale(resolved)
                        .build());

        draft.setPayload(payload);
        contentDraftDao.save(draft);
    }

    @Override
    @Transactional
    public void deleteDraft(String userId, String slug, String locale) {
        var normalizedSlug = SlugNormalizer.normalizeSlug(slug);
        var resolved = localeResolver.resolveForWrite(locale);
        logger.info("Deleting draft for slug: {} locale: {} user: {}", normalizedSlug, resolved, userId);

        contentDraftDao.deleteOwn(normalizedSlug, userId, resolved);
    }


    @Override
    @Transactional
    public SyncResultDto sync(List<SyncManifestRequestDto> manifests, List<String> locales) {
        var declared = localeResolver.replaceDeclared(locales);
        logger.info("Sync started with {} manifest(s), locales={}", manifests.size(), declared);


        var lanes = declared.isEmpty() ? Collections.<String>singletonList(null) : declared;

        var allBlocks = new ArrayList<>(contentBlockDao.findAll());
        adoptLegacyRows(allBlocks, declared);

        var blocksByKey = allBlocks.stream()
                .collect(Collectors.toMap(
                        b -> key(b.getSlug(), b.getBlockPath(), b.getLocale()),
                        Function.identity()));

        var seenKeys = new HashSet<String>();
        var manifestSlugs = new HashSet<String>();
        var toSave = new ArrayList<ContentBlock>();
        var results = new ArrayList<SyncResultDto.SyncSlugResultDto>();

        for (var manifest : manifests) {
            var manifestSlug = SlugNormalizer.normalizeSlug(manifest.getSlug());
            manifestSlugs.add(manifestSlug);

            var manifestBlocks = manifest.getBlocks() == null
                    ? List.<SyncManifestRequestDto.ManifestBlockDto>of()
                    : manifest.getBlocks();

            int created = 0, restored = 0, unchanged = 0;

            int index = 0;
            for (var mb : manifestBlocks) {
                var blockPath = SlugNormalizer.normalizeBlockPath(mb.getBlockPath());
                var sortOrder = mb.getSortOrder() == null ? index : mb.getSortOrder();
                index++;


                for (var lane : lanes) {
                    var k = key(manifestSlug, blockPath, lane);
                    seenKeys.add(k);

                    var existing = blocksByKey.get(k);

                    if (existing == null) {

                        var block = ContentBlock.builder()
                                .slug(manifestSlug)
                                .blockPath(blockPath)
                                .locale(lane)
                                .blockType(mb.getBlockType())
                                .value(mb.getDefaultValue() == null ? objectMapper.nullNode() : mb.getDefaultValue())
                                .itemSchema(mb.getItemSchema())
                                .sortOrder(sortOrder)
                                .updatedBy(CmsMessages.SYNCED_BY_DEPLOY_PIPELINE)
                                .build();
                        toSave.add(block);
                        created++;
                        continue;
                    }

                    boolean changed = false;

                    if (existing.isArchived()) {
                        existing.setArchived(false);
                        existing.setArchivedAt(null);
                        changed = true;
                        restored++;
                    }

                    if (existing.getBlockType() != mb.getBlockType()
                            || existing.getSortOrder() != sortOrder
                            || !Objects.equals(existing.getItemSchema(), mb.getItemSchema())) {
                        existing.setBlockType(mb.getBlockType());
                        existing.setSortOrder(sortOrder);
                        existing.setItemSchema(mb.getItemSchema());
                        changed = true;
                    }

                    if (changed) {
                        touch(existing, CmsMessages.SYNCED_BY_DEPLOY_PIPELINE);
                        toSave.add(existing);
                    } else {
                        unchanged++;
                    }
                }
            }

            results.add(new SyncResultDto.SyncSlugResultDto(manifestSlug, created, 0, unchanged, restored));
        }

        var deletedBySlug = new HashMap<String, Integer>();
        var prunedSlugs = new TreeSet<String>();

        for (var block : allBlocks) {
            if (seenKeys.contains(key(block.getSlug(), block.getBlockPath(), block.getLocale()))) continue;
            if (block.isArchived()) continue;

            block.setArchived(true);
            block.setArchivedAt(Instant.now());
            touch(block, CmsMessages.SYNCED_BY_DEPLOY_PIPELINE);
            toSave.add(block);

            deletedBySlug.merge(block.getSlug(), 1, Integer::sum);
            if (!manifestSlugs.contains(block.getSlug())) {
                prunedSlugs.add(block.getSlug());
            }
        }

        for (var result : results) {
            result.setDeleted(deletedBySlug.getOrDefault(result.getSlug(), 0));
        }
        for (var entry : deletedBySlug.entrySet()) {
            if (!manifestSlugs.contains(entry.getKey())) {
                results.add(new SyncResultDto.SyncSlugResultDto(entry.getKey(), 0, entry.getValue(), 0, 0));
            }
        }

        contentBlockDao.saveAll(toSave);

        logger.info("Sync finished: {} slug result(s), {} pruned slug(s)", results.size(), prunedSlugs.size());

        return new SyncResultDto(results, new ArrayList<>(prunedSlugs));
    }


    private void adoptLegacyRows(List<ContentBlock> blocks, List<String> declared) {
        if (declared.isEmpty()) {
            return;
        }

        var first = declared.get(0);
        var taken = blocks.stream()
                .filter(block -> first.equals(block.getLocale()))
                .map(block -> key(block.getSlug(), block.getBlockPath(), first))
                .collect(Collectors.toSet());

        var adopted = new ArrayList<ContentBlock>();
        for (var block : blocks) {
            if (block.getLocale() != null) continue;
            if (taken.contains(key(block.getSlug(), block.getBlockPath(), first))) continue;

            block.setLocale(first);
            adopted.add(block);
        }

        if (!adopted.isEmpty()) {
            contentBlockDao.saveAll(adopted);
            logger.info("Adopted {} pre-localization block(s) into locale {}", adopted.size(), first);
        }
    }


    private void touch(ContentBlock block, String updatedBy) {
        block.setUpdatedBy(updatedBy);
        block.setVersion(block.getVersion() + 1);
    }

    private List<BlockDto> toBlockDtos(List<ContentBlock> blocks) {
        return blocks.stream()
                .map(b -> new BlockDto(
                        b.getBlockPath(),
                        b.getBlockType(),
                        b.getValue(),
                        b.getSortOrder(),
                        b.getVersion()))
                .collect(Collectors.toList());
    }

    private Map<String, JsonNode> parseDraftPayload(JsonNode payload) {
        var map = new HashMap<String, JsonNode>();
        if (payload != null && payload.isArray()) {
            for (JsonNode node : payload) {
                var path = node.path("blockPath").asString(null);
                if (path != null && !path.isBlank() && node.has("value")) {
                    map.put(SlugNormalizer.normalizeBlockPath(path), node.get("value"));
                }
            }
        }
        return map;
    }

    private String key(String slug, String blockPath, String locale) {
        return slug + "\u0000" + blockPath + "\u0000" + (locale == null ? "" : locale);
    }
}
