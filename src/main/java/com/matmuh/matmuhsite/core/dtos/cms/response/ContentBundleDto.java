package com.matmuh.matmuhsite.core.dtos.cms.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ContentBundleDto(String locale, List<ContentPageDto> global, List<ContentPageDto> pages) {

    public record ContentPageDto(String slug, List<BlockDto> blocks) {
    }
}
