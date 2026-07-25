package com.matmuh.matmuhsite.dataAccess.abstracts.cms;

import com.matmuh.matmuhsite.entities.cms.CollectionItem;

import java.util.List;

public interface CollectionItemDaoCustom {

    List<CollectionItem> searchByFilter(String collectionKey, String filterJson, int offset, int limit);

    long countByFilter(String collectionKey, String filterJson);
}
