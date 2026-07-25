package com.matmuh.matmuhsite.core.dtos.cms.response;

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
    public static class UploadDataDto {
        private String url;
    }

    public static UploadResponseDto of(String url) {
        return new UploadResponseDto(new UploadDataDto(url));
    }
}