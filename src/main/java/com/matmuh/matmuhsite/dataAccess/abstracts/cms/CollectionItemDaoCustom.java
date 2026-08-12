package com.matmuh.matmuhsite.dataAccess.abstracts.cms;

import com.matmuh.matmuhsite.core.helpers.CollectionSortParser.CollectionSort;
import com.matmuh.matmuhsite.entities.cms.CollectionItem;

import java.util.List;

public interface CollectionItemDaoCustom {

    List<CollectionItem> searchByFilter(String collectionKey, String filterJson, CollectionSort sort,
                                        boolean archived, String locale, int offset, int limit);

    long countByFilter(String collectionKey, String filterJson, boolean archived, String locale);
}
