package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionItemDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionListDto;
import tools.jackson.databind.node.ObjectNode;

public interface CmsCollectionProvider {

    String collectionKey();

    CollectionListDto list(ObjectNode filters, int offset, int limit);

    CollectionItemDto getBySlug(String slug);

    CollectionItemDto create(ObjectNode data);

    CollectionItemDto upsert(String slug, ObjectNode data, Integer version);

    boolean existsBySlug(String slug);
}
