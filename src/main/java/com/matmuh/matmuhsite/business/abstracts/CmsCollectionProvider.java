package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionItemDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionListDto;
import tools.jackson.databind.node.ObjectNode;

public interface CmsCollectionProvider {

    String collectionKey();

    CollectionListDto list(ObjectNode filters, String locale, int offset, int limit);

    CollectionItemDto getBySlug(String slug, String locale);

    CollectionItemDto create(ObjectNode data, String locale);

    CollectionItemDto upsert(String slug, ObjectNode data, Integer version, String locale);

    boolean existsBySlug(String slug);
}
