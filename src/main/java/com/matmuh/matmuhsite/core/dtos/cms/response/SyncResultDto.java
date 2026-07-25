package com.matmuh.matmuhsite.core.dtos.cms.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SyncResultDto {

    private List<SyncSlugResultDto> results;

    private List<String> prunedSlugs;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SyncSlugResultDto {
        private String slug;
        private int created;
        private int deleted;
        private int unchanged;
        private int restored;
    }
}
