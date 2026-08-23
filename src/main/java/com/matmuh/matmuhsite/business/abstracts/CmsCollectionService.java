package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.dtos.cms.request.CreateCollectionItemRequestDto;
import com.matmuh.matmuhsite.core.dtos.cms.request.SaveDraftRequestDto;
import com.matmuh.matmuhsite.core.dtos.cms.request.RenameSlugRequestDto;
import com.matmuh.matmuhsite.core.dtos.cms.request.SaveNewDraftRequestDto;
import com.matmuh.matmuhsite.core.dtos.cms.request.UpsertCollectionItemRequestDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.ArchiveResultDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionItemDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionListDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.MyCollectionDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionSchema;
import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionSchemaResponseDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CmsCollectionService {

    boolean allowsAnonymousRead(String collectionKey);

    CollectionSchemaResponseDto getSchema(String collectionKey);

    List<MyCollectionDto> getMyCollections();

    CollectionListDto list(String collectionKey, String userId, Map<String, String> filters,
                           String sort, boolean archived, String locale, String search, int offset, int limit);

    CollectionItemDto getBySlug(String collectionKey, String slug, String userId, String locale);

    CollectionItemDto upsert(String collectionKey, String slug, UpsertCollectionItemRequestDto request, String updatedBy, String locale, UUID translationGroup);

    CollectionItemDto createWithAutoSlug(String collectionKey, CreateCollectionItemRequestDto request, String updatedBy, String locale, UUID translationGroup);

    void saveItemDraft(String collectionKey, String slug, String userId, SaveDraftRequestDto request, String locale);

    void saveNewDraft(String collectionKey, String userId, SaveNewDraftRequestDto request, String locale);

    ArchiveResultDto archive(String collectionKey, String slug, Integer version, String updatedBy);

    CollectionItemDto restore(String collectionKey, String slug, String updatedBy);

    void deleteItemDraft(String collectionKey, String slug, String userId, String locale);

    void deleteNewDraft(String collectionKey, String userId, String locale);

    CollectionItemDto renameSlug(String collectionKey, String slug, RenameSlugRequestDto request,
                                 boolean replaceAlias, String updatedBy);

    void deleteSlugAlias(String collectionKey, String slug);
}
