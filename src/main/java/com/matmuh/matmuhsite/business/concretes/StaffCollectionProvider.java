package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.CmsCollectionProvider;
import com.matmuh.matmuhsite.business.abstracts.StaffService;
import com.matmuh.matmuhsite.business.constants.CmsMessages;
import com.matmuh.matmuhsite.business.constants.StaffCollectionSchema;
import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionItemDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionListDto;
import com.matmuh.matmuhsite.core.dtos.staff.request.CreateStaffRequestDto;
import com.matmuh.matmuhsite.core.dtos.staff.request.UpdateStaffRequestDto;
import com.matmuh.matmuhsite.core.dtos.staff.response.StaffDto;
import com.matmuh.matmuhsite.core.exceptions.CmsValidationException;
import com.matmuh.matmuhsite.core.exceptions.ConcurrencyConflictException;
import com.matmuh.matmuhsite.core.exceptions.ResourceNotFoundException;
import com.matmuh.matmuhsite.core.helpers.OffsetPageable;
import com.matmuh.matmuhsite.core.mappers.StaffMapper;
import com.matmuh.matmuhsite.dataAccess.abstracts.StaffDao;
import com.matmuh.matmuhsite.entities.Staff;
import com.matmuh.matmuhsite.entities.StaffGroup;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Validator;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
public class StaffCollectionProvider implements CmsCollectionProvider {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "lastName");

    private final StaffService staffService;
    private final StaffDao staffDao;
    private final StaffMapper staffMapper;
    private final JsonMapper objectMapper;
    private final Validator validator;


    @Override
    public String collectionKey() {
        return StaffCollectionSchema.KEY;
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionListDto list(ObjectNode filters, String locale, int offset, int limit) {
        var page = staffDao.filter(
                filterText(filters, StaffCollectionSchema.FIELD_FIRST_NAME),
                filterText(filters, StaffCollectionSchema.FIELD_LAST_NAME),
                filterText(filters, StaffCollectionSchema.FIELD_EMAIL),
                filterText(filters, StaffCollectionSchema.FIELD_AVESIS_LINK),
                filterGroup(filters),
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
        var request = convert(data, CreateStaffRequestDto.class);
        validate(request);
        return toItem(staffService.createStaff(request));
    }




    @Override
    @Transactional
    public CollectionItemDto upsert(String slug, ObjectNode data, Integer version, String locale) {
        var staff = requireBySlug(slug);

        if (version != null && version != staff.getVersion()){
            throw new ConcurrencyConflictException(CmsMessages.VERSION_CONFLICT);
        }

        var request = convert(data, UpdateStaffRequestDto.class);
        validate(request);
        return toItem(staffService.updateStaff(staff.getId(), request));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySlug(String slug) {
        return staffDao.existsBySlug(slug);

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

    private StaffGroup filterGroup(ObjectNode filters) {
        var node = filters == null ? null : filters.get(StaffCollectionSchema.FIELD_GROUPS);
        if (node == null || node.isNull()) {
            return null;
        }

        var text = node.isArray() ? (node.size() == 0 ? null : node.get(0).asString()) : node.asString();
        if (text == null || text.isBlank()) {
            return null;
        }

        try {
            return StaffGroup.valueOf(text.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new CmsValidationException("Field '" + StaffCollectionSchema.FIELD_GROUPS + "': invalid value '" + text + "'.");
        }
    }

    private CollectionItemDto toItem(Staff staff) {
        return toItem(staffMapper.toStaffDto(staff), staff.getVersion());
    }

    private CollectionItemDto toItem(StaffDto staff) {
        var version = staffDao.findBySlug(staff.getSlug()).map(Staff::getVersion).orElse(0);
        return toItem(staff, version);
    }



    private CollectionItemDto toItem(StaffDto staff, int version) {
        var item = new CollectionItemDto();
        item.setId(staff.getId());
        item.setCollectionKey(StaffCollectionSchema.KEY);
        item.setSlug(staff.getSlug());
        item.setData(withDisplayName(objectMapper.valueToTree(staff), staff));
        item.setVersion(version);
        return item;
    }

    private Staff requireBySlug(String slug) {
        return staffDao.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        CmsMessages.COLLECTION_ITEM_NOT_FOUND + StaffCollectionSchema.KEY + "/" + slug
                ));


    }

    // displayField referans seçicilerinin okuduğu ad. StaffDto'ya konmuyor:
    // /api/staff sözleşmesini değiştirmeden yalnız CMS tarafında türetiliyor.
    private JsonNode withDisplayName(JsonNode node, StaffDto staff) {
        if (!(node instanceof ObjectNode object)) {
            return node;
        }

        var name = Stream.of(staff.getAcademicTitle(), staff.getFirstName(), staff.getLastName())
                .filter(part -> part != null && !part.isBlank())
                .map(String::trim)
                .collect(Collectors.joining(" "));

        object.put(StaffCollectionSchema.DISPLAY_FIELD, name.isBlank() ? staff.getSlug() : name);
        return object;
    }
}
