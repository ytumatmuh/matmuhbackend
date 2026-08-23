package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.CmsCollectionProvider;
import com.matmuh.matmuhsite.business.abstracts.ElectiveGroupService;
import com.matmuh.matmuhsite.business.constants.CmsMessages;
import com.matmuh.matmuhsite.business.constants.ElectiveGroupCollectionSchema;
import com.matmuh.matmuhsite.business.constants.ElectiveGroupMessages;
import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionItemDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionListDto;
import com.matmuh.matmuhsite.core.dtos.electiveGroup.request.CreateElectiveGroupRequestDto;
import com.matmuh.matmuhsite.core.dtos.electiveGroup.request.UpdateElectiveGroupRequestDto;
import com.matmuh.matmuhsite.core.dtos.electiveGroup.response.ElectiveGroupDto;
import com.matmuh.matmuhsite.core.exceptions.CmsValidationException;
import com.matmuh.matmuhsite.core.exceptions.ConcurrencyConflictException;
import com.matmuh.matmuhsite.core.exceptions.ResourceNotFoundException;
import com.matmuh.matmuhsite.core.helpers.OffsetPageable;
import com.matmuh.matmuhsite.dataAccess.abstracts.ElectiveGroupDao;
import com.matmuh.matmuhsite.entities.DegreeLevel;
import com.matmuh.matmuhsite.entities.ElectiveGroup;
import com.matmuh.matmuhsite.entities.Semester;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ElectiveGroupCollectionProvider implements CmsCollectionProvider {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "code");

    private final ElectiveGroupService electiveGroupService;
    private final ElectiveGroupDao electiveGroupDao;
    private final JsonMapper objectMapper;
    private final Validator validator;

    @Override
    public String collectionKey() {
        return ElectiveGroupCollectionSchema.KEY;
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionListDto list(ObjectNode filters, String locale, int offset, int limit) {
        var page = electiveGroupDao.search(
                filterTerm(filters),
                filterSemester(filters),
                filterDegreeLevel(filters),
                filterSearch(filters),
                OffsetPageable.of(offset, limit, DEFAULT_SORT));

        var items = page.getContent().stream()
                .map(group -> toItem(electiveGroupService.getElectiveGroupById(group.getId()), group.getVersion()))
                .toList();

        return new CollectionListDto(items, page.getTotalElements(), offset, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionItemDto getBySlug(String slug, String locale) {
        var group = requireBySlug(slug);
        return toItem(electiveGroupService.getElectiveGroupById(group.getId()), group.getVersion());
    }

    @Override
    @Transactional
    public CollectionItemDto create(ObjectNode data, String locale) {
        var request = convert(data, CreateElectiveGroupRequestDto.class);
        validate(request);
        return toItem(electiveGroupService.createElectiveGroup(request));
    }

    @Override
    @Transactional
    public CollectionItemDto upsert(String slug, ObjectNode data, Integer version, String locale) {
        var group = requireBySlug(slug);

        if (version != null && version != group.getVersion()) {
            throw new ConcurrencyConflictException(CmsMessages.VERSION_CONFLICT);
        }

        var request = convert(data, UpdateElectiveGroupRequestDto.class);
        validate(request);
        return toItem(electiveGroupService.updateElectiveGroup(group.getId(), request));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySlug(String slug) {
        return electiveGroupDao.existsBySlug(slug);
    }

    private ElectiveGroup requireBySlug(String slug) {
        return electiveGroupDao.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        CmsMessages.COLLECTION_ITEM_NOT_FOUND + ElectiveGroupCollectionSchema.KEY + "/" + slug));
    }

    private <T> T convert(ObjectNode data, Class<T> type) {
        try {
            return objectMapper.treeToValue(data, type);
        } catch (RuntimeException e) {
            throw new CmsValidationException(e.getMessage());
        }
    }

    private <T> void validate(T request) {
        var violations = validator.validate(request);
        if (violations.isEmpty()) {
            return;
        }
        throw new CmsValidationException(violations.stream()
                .map(violation -> "Field '" + violation.getPropertyPath() + "': " + violation.getMessage())
                .sorted()
                .toList());
    }

    private Integer filterTerm(ObjectNode filters) {
        var node = filters == null ? null : filters.get(ElectiveGroupCollectionSchema.FIELD_TERM);
        return node == null || node.isNull() ? null : node.asInt();
    }

    private String filterSearch(ObjectNode filters) {
        if (filters == null) {
            return null;
        }
        var node = filters.get(ElectiveGroupCollectionSchema.FIELD_CODE);
        if (node == null || node.isNull()) {
            node = filters.get(ElectiveGroupCollectionSchema.FIELD_NAME);
        }
        return node == null || node.isNull() ? null : node.asString();
    }

    private Semester filterSemester(ObjectNode filters) {
        var node = filters == null ? null : filters.get(ElectiveGroupCollectionSchema.FIELD_SEMESTER);
        if (node == null || node.isNull()) {
            return null;
        }
        try {
            return Semester.valueOf(node.asString().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new CmsValidationException("Field '" + ElectiveGroupCollectionSchema.FIELD_SEMESTER
                    + "': invalid value '" + node.asString() + "'.");
        }
    }

    private DegreeLevel filterDegreeLevel(ObjectNode filters) {
        var node = filters == null ? null : filters.get(ElectiveGroupCollectionSchema.FIELD_DEGREE_LEVELS);
        if (node == null || node.isNull()) {
            return null;
        }

        var text = node.isArray() ? (node.isEmpty() ? null : node.get(0).asString()) : node.asString();
        if (text == null || text.isBlank()) {
            return null;
        }

        try {
            return DegreeLevel.valueOf(text.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new CmsValidationException(ElectiveGroupMessages.DEGREE_LEVEL_INVALID);
        }
    }

    private CollectionItemDto toItem(ElectiveGroupDto group) {
        var version = electiveGroupDao.findBySlug(group.getSlug()).map(ElectiveGroup::getVersion).orElse(0);
        return toItem(group, version);
    }

    private CollectionItemDto toItem(ElectiveGroupDto group, int version) {
        var item = new CollectionItemDto();
        item.setId(group.getId());
        item.setCollectionKey(ElectiveGroupCollectionSchema.KEY);
        item.setSlug(group.getSlug());
        item.setData(objectMapper.valueToTree(group));
        item.setVersion(version);
        return item;
    }
}
