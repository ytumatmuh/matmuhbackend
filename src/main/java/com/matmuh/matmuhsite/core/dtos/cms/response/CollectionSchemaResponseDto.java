package com.matmuh.matmuhsite.core.dtos.cms.response;

import com.matmuh.matmuhsite.entities.cms.SlugSource;

import java.util.List;


public record CollectionSchemaResponseDto(
        String collectionKey,
        CollectionSchema schema,
        SlugSource slugSource,
        boolean slugEditable,
        List<String> locales,
        String displayField
) {
}
