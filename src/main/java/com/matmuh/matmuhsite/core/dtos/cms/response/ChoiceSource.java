package com.matmuh.matmuhsite.core.dtos.cms.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChoiceSource(String kind, List<String> values, String collection) {

    public static final String STATIC = "static";
    public static final String COLLECTION = "collection";

    public static ChoiceSource ofValues(String... values) {
        return new ChoiceSource(STATIC, List.of(values), null);
    }

    public static ChoiceSource ofCollection(String collectionKey) {
        return new ChoiceSource(COLLECTION, null, collectionKey);
    }

    public boolean pointsToCollection() {
        return COLLECTION.equals(kind);
    }
}
