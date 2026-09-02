package com.matmuh.matmuhsite.core.dtos.cms.response;

import java.util.List;

public record CollectionLookupDto(List<Item> items, long total) {

    public record Item(String slug, String label) {
    }
}
