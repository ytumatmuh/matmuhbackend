package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.CmsCollectionProvider;
import com.matmuh.matmuhsite.business.abstracts.LectureService;
import com.matmuh.matmuhsite.business.constants.CmsMessages;
import com.matmuh.matmuhsite.business.constants.LectureCollectionSchema;
import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionItemDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionListDto;
import com.matmuh.matmuhsite.core.dtos.lecture.request.CreateLectureRequestDto;
import com.matmuh.matmuhsite.core.dtos.lecture.request.UpdateLectureRequestDto;
import com.matmuh.matmuhsite.core.dtos.lecture.response.LectureDto;
import com.matmuh.matmuhsite.core.exceptions.CmsValidationException;
import com.matmuh.matmuhsite.core.exceptions.ConcurrencyConflictException;
import com.matmuh.matmuhsite.core.exceptions.ResourceNotFoundException;
import com.matmuh.matmuhsite.core.helpers.OffsetPageable;
import com.matmuh.matmuhsite.core.mappers.LectureMapper;
import com.matmuh.matmuhsite.dataAccess.abstracts.LectureDao;
import com.matmuh.matmuhsite.entities.Lecture;
import com.matmuh.matmuhsite.entities.Semester;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

@Service
@RequiredArgsConstructor
public class LectureCollectionProvider implements CmsCollectionProvider {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "code");

    private static final String SEMESTER_INVALID =
            "Field 'semester' must be one of FALL, SPRING, SUMMER.";

    private final LectureService lectureService;
    private final LectureDao lectureDao;
    private final LectureMapper lectureMapper;
    private final JsonMapper objectMapper;
    private final Validator validator;


    @Override
    public String collectionKey() {
        return LectureCollectionSchema.KEY;
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionListDto list(ObjectNode filters, String locale, int offset, int limit) {
        var page = lectureDao.search(
                filterTerm(filters),
                filterSemester(filters),
                filterCode(filters),
                OffsetPageable.of(offset, limit, DEFAULT_SORT));

        var items = page.getContent().stream().map(this::toItem).toList();

        return new CollectionListDto(items, page.getTotalElements(), offset, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionItemDto getBySlug(String slug, String locale) {
        return toItem(requireBySlug(slug));
    }

    @Override
    @Transactional
    public CollectionItemDto create(ObjectNode data, String locale) {
        var request = convert(data, CreateLectureRequestDto.class);
        validate(request);
        return toItem(lectureService.createLecture(request));
    }

    @Override
    @Transactional
    public CollectionItemDto upsert(String slug, ObjectNode data, Integer version, String locale) {
        var lecture = requireBySlug(slug);

        if (version != null && version != lecture.getVersion()) {
            throw new ConcurrencyConflictException(CmsMessages.VERSION_CONFLICT);
        }

        var request = convert(data, UpdateLectureRequestDto.class);
        validate(request);
        return toItem(lectureService.updateLecture(lecture.getId(), request));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySlug(String slug) {
        return lectureDao.existsBySlug(slug);
    }

    private Lecture requireBySlug(String slug) {
        return lectureDao.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        CmsMessages.COLLECTION_ITEM_NOT_FOUND + LectureCollectionSchema.KEY + "/" + slug));
    }

    private CollectionItemDto toItem(Lecture lecture) {
        return toItem(lectureMapper.toDto(lecture), lecture.getVersion());
    }

    private CollectionItemDto toItem(LectureDto lecture) {
        var version = lectureDao.findBySlug(lecture.getSlug()).map(Lecture::getVersion).orElse(0);
        return toItem(lecture, version);
    }

    private CollectionItemDto toItem(LectureDto lecture, int version) {
        var item = new CollectionItemDto();
        item.setId(lecture.getId());
        item.setCollectionKey(LectureCollectionSchema.KEY);
        item.setSlug(lecture.getSlug());
        item.setData(objectMapper.valueToTree(lecture));
        item.setVersion(version);
        return item;
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
        var node = filters == null ? null : filters.get(LectureCollectionSchema.FIELD_TERM);
        return node == null || node.isNull() ? null : node.asInt();
    }

    private Semester filterSemester(ObjectNode filters) {
        var node = filters == null ? null : filters.get(LectureCollectionSchema.FIELD_SEMESTER);
        if (node == null || node.isNull()) {
            return null;
        }
        try {
            return Semester.valueOf(node.asString().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CmsValidationException(SEMESTER_INVALID);
        }
    }

    private String filterCode(ObjectNode filters) {
        var node = filters == null ? null : filters.get(LectureCollectionSchema.FIELD_CODE);
        return node == null || node.isNull() ? null : node.asString();
    }
}
