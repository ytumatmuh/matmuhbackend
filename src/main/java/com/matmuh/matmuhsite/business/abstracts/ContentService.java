package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.dtos.cms.request.SyncManifestRequestDto;
import com.matmuh.matmuhsite.core.dtos.cms.request.UpdatePageRequestDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.ContentResponseDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.SyncResultDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.UpdatePageResponseDto;

import java.util.List;

public interface ContentService {

    ContentResponseDto getPublishedBySlug(String slug, String locale);

    ContentResponseDto getBySlugForEditor(String userId, String slug, String locale);

    UpdatePageResponseDto updatePage(String userId, UpdatePageRequestDto request, String locale);

    void saveDraft(String userId, UpdatePageRequestDto request, String locale);

    void deleteDraft(String userId, String slug, String locale);

    SyncResultDto sync(List<SyncManifestRequestDto> manifests, List<String> locales);
}
