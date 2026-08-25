package com.matmuh.matmuhsite.core.dtos.cms.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UploadResponseDto {

    private UploadDataDto data;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UploadDataDto {
        private String url;
        private String previewUrl;
    }

    public static UploadResponseDto of(String url, String previewUrl) {
        return new UploadResponseDto(new UploadDataDto(url, previewUrl));
    }
}