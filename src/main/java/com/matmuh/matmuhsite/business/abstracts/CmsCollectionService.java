package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.dtos.cms.request.CreateCollectionItemRequestDto;
import com.matmuh.matmuhsite.core.dtos.cms.request.SaveDraftRequestDto;
import com.matmuh.matmuhsite.core.dtos.cms.request.SaveNewDraftRequestDto;
import com.matmuh.matmuhsite.core.dtos.cms.request.UpsertCollectionItemRequestDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionItemDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionListDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.MyCollectionDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionSchema;

import java.util.List;
import java.util.Map;

public interface CmsCollectionService {

    boolean allowsAnonymousRead(String collectionKey);

    CollectionSchema getSchema(String collectionKey);

    List<MyCollectionDto> getMyCollections();

    CollectionListDto list(String collectionKey, String userId, Map<String, String> filters, int offset, int limit);

    CollectionItemDto getBySlug(String collectionKey, String slug, String userId);

    CollectionItemDto upsert(String collectionKey, String slug, UpsertCollectionItemRequestDto request, String updatedBy);

    CollectionItemDto createWithAutoSlug(String collectionKey, CreateCollectionItemRequestDto request, String updatedBy);

    void saveItemDraft(String collectionKey, String slug, String userId, SaveDraftRequestDto request);

    void saveNewDraft(String collectionKey, String userId, SaveNewDraftRequestDto request);

    void deleteItemDraft(String collectionKey, String slug, String userId);

    void deleteNewDraft(String collectionKey, String userId);
}
