package com.matmuh.matmuhsite.core.dtos.cms.response;

import java.util.List;

public record CollectionSchema(List<FieldDefinition> fields) {

    public static CollectionSchema of(FieldDefinition... fields) {
        return new CollectionSchema(List.of(fields));
    }
}
