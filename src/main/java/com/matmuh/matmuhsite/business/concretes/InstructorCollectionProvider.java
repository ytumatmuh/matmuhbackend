package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.CmsCollectionProvider;
import com.matmuh.matmuhsite.business.abstracts.InstructorService;
import com.matmuh.matmuhsite.business.constants.CmsMessages;
import com.matmuh.matmuhsite.business.constants.InstructorCollectionSchema;
import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionItemDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionListDto;
import com.matmuh.matmuhsite.core.dtos.instructor.request.CreateInstructorRequestDto;
import com.matmuh.matmuhsite.core.dtos.instructor.request.UpdateInstructorRequestDto;
import com.matmuh.matmuhsite.core.dtos.instructor.response.InstructorDto;
import com.matmuh.matmuhsite.core.exceptions.CmsValidationException;
import com.matmuh.matmuhsite.core.exceptions.ConcurrencyConflictException;
import com.matmuh.matmuhsite.core.exceptions.ResourceNotFoundException;
import com.matmuh.matmuhsite.core.helpers.OffsetPageable;
import com.matmuh.matmuhsite.core.mappers.InstructorMapper;
import com.matmuh.matmuhsite.dataAccess.abstracts.InstructorDao;
import com.matmuh.matmuhsite.entities.Instructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Validator;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

@Service
@RequiredArgsConstructor
public class InstructorCollectionProvider implements CmsCollectionProvider {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "lastName");

    private final InstructorService instructorService;
    private final InstructorDao instructorDao;
    private final InstructorMapper instructorMapper;
    private final JsonMapper objectMapper;
    private final Validator validator;


    @Override
    public String collectionKey() {
        return InstructorCollectionSchema.KEY;
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionListDto list(ObjectNode filters, String locale, int offset, int limit) {
        var page = instructorDao.filter(
                filterText(filters, InstructorCollectionSchema.FIELD_FIRST_NAME),
                filterText(filters, InstructorCollectionSchema.FIELD_LAST_NAME),
                filterText(filters, InstructorCollectionSchema.FIELD_EMAIL),
                filterText(filters, InstructorCollectionSchema.FIELD_AVESIS_LINK),
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
        var request = convert(data, CreateInstructorRequestDto.class);
        validate(request);
        return toItem(instructorService.createInstructor(request));
    }




    @Override
    @Transactional
    public CollectionItemDto upsert(String slug, ObjectNode data, Integer version, String locale) {
        var instructor = requireBySlug(slug);

        if (version != null && version != instructor.getVersion()){
            throw new ConcurrencyConflictException(CmsMessages.VERSION_CONFLICT);
        }

        var request = convert(data, UpdateInstructorRequestDto.class);
        validate(request);
        return toItem(instructorService.updateInstructor(instructor.getId(), request));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySlug(String slug) {
        return instructorDao.existsBySlug(slug);

    }

    private <T> T convert(ObjectNode data, Class<T> type) {
        try {
            return objectMapper.treeToValue(data, type);
        }catch (RuntimeException e){
            throw new CmsValidationException(e.getMessage());
        }
    }

    private <T> void validate(T request) {
        var violations = validator.validate(request);
        if (violations.isEmpty()){
            return;
        }

        throw new CmsValidationException(violations.stream()
                .map(violation -> "Field '" + violation.getPropertyPath() + "': " + violation.getMessage())
                .sorted()
                .toList());

    }


    private String filterText(ObjectNode filters, String field) {
        var node = filters == null ? null : filters.get(field);
        return node == null || node.isNull() ? null : node.asString();
    }

    private CollectionItemDto toItem(Instructor instructor) {
        return toItem(instructorMapper.toInstructorDto(instructor), instructor.getVersion());
    }

    private CollectionItemDto toItem(InstructorDto instructor) {
        var version = instructorDao.findBySlug(instructor.getSlug()).map(Instructor::getVersion).orElse(0);
        return toItem(instructor, version);
    }



    private CollectionItemDto toItem(InstructorDto instructor, int version) {
        var item = new CollectionItemDto();
        item.setId(instructor.getId());
        item.setCollectionKey(InstructorCollectionSchema.KEY);
        item.setSlug(instructor.getSlug());
        item.setData(objectMapper.valueToTree(instructor));
        item.setVersion(version);
        return item;
    }

    private Instructor requireBySlug(String slug) {
        return instructorDao.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        CmsMessages.COLLECTION_ITEM_NOT_FOUND + InstructorCollectionSchema.KEY + "/" + slug
                ));


    }
}
