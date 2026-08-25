package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.CalendarAdminService;
import com.matmuh.matmuhsite.business.abstracts.CmsCollectionProvider;
import com.matmuh.matmuhsite.business.constants.AcademicTermCollectionSchema;
import com.matmuh.matmuhsite.business.constants.CmsMessages;
import com.matmuh.matmuhsite.core.dtos.calendar.request.SaveAcademicTermRequestDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionItemDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionListDto;
import com.matmuh.matmuhsite.core.exceptions.CmsValidationException;
import com.matmuh.matmuhsite.core.exceptions.ResourceNotFoundException;
import com.matmuh.matmuhsite.dataAccess.abstracts.AcademicTermDao;
import com.matmuh.matmuhsite.entities.AcademicTerm;
import com.matmuh.matmuhsite.entities.Semester;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Locale;

@Service
public class AcademicTermCollectionProvider implements CmsCollectionProvider {

    private final CalendarAdminService calendarAdminService;
    private final AcademicTermDao academicTermDao;
    private final JsonMapper objectMapper;
    private final Validator validator;

    public AcademicTermCollectionProvider(CalendarAdminService calendarAdminService,
                                          AcademicTermDao academicTermDao,
                                          JsonMapper objectMapper,
                                          Validator validator) {
        this.calendarAdminService = calendarAdminService;
        this.academicTermDao = academicTermDao;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }


    public static String slugOf(String academicYear, Semester semester) {
        return academicYear + "-" + semester.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public String collectionKey() {
        return AcademicTermCollectionSchema.KEY;
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionListDto list(ObjectNode filters, String locale, int offset, int limit) {
        var terms = academicTermDao.findAllByOrderByStartDateDesc();

        var page = terms.stream()
                .skip(offset)
                .limit(limit)
                .map(this::toItem)
                .toList();

        return new CollectionListDto(page, terms.size(), offset, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionItemDto getBySlug(String slug, String locale) {
        return toItem(requireBySlug(slug));
    }

    @Override
    @Transactional
    public CollectionItemDto create(ObjectNode data, String locale) {
        return toItem(calendarAdminService.saveTerm(request(data)));
    }

    @Override
    @Transactional
    public CollectionItemDto upsert(String slug, ObjectNode data, Integer version, String locale) {
        requireBySlug(slug);
        return toItem(calendarAdminService.saveTerm(request(data)));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySlug(String slug) {
        return findBySlug(slug).isPresent();
    }

    private AcademicTerm requireBySlug(String slug) {
        return findBySlug(slug).orElseThrow(() -> new ResourceNotFoundException(
                CmsMessages.COLLECTION_ITEM_NOT_FOUND + AcademicTermCollectionSchema.KEY + "/" + slug));
    }

    private java.util.Optional<AcademicTerm> findBySlug(String slug) {
        return academicTermDao.findAllByOrderByStartDateDesc().stream()
                .filter(term -> slugOf(term.getAcademicYear(), term.getSemester()).equalsIgnoreCase(slug))
                .findFirst();
    }

    private SaveAcademicTermRequestDto request(ObjectNode data) {
        SaveAcademicTermRequestDto request;
        try {
            request = objectMapper.treeToValue(data, SaveAcademicTermRequestDto.class);
        } catch (RuntimeException exception) {
            throw new CmsValidationException(exception.getMessage());
        }

        var violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new CmsValidationException(violations.stream()
                    .map(violation -> "Field '" + violation.getPropertyPath() + "': " + violation.getMessage())
                    .sorted()
                    .toList());
        }

        return request;
    }

    private CollectionItemDto toItem(AcademicTerm term) {
        var data = objectMapper.createObjectNode();
        data.put(AcademicTermCollectionSchema.FIELD_ACADEMIC_YEAR, term.getAcademicYear());
        data.put(AcademicTermCollectionSchema.FIELD_SEMESTER, term.getSemester().name());
        data.put(AcademicTermCollectionSchema.FIELD_START_DATE, term.getStartDate().toString());
        data.put(AcademicTermCollectionSchema.FIELD_END_DATE, term.getEndDate().toString());

        var item = new CollectionItemDto();
        item.setId(term.getId());
        item.setCollectionKey(AcademicTermCollectionSchema.KEY);
        item.setSlug(slugOf(term.getAcademicYear(), term.getSemester()));
        item.setData(data);
        item.setVersion(0);
        return item;
    }
}
