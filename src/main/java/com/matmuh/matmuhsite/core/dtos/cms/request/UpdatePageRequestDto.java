package com.matmuh.matmuhsite.core.dtos.cms.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.matmuh.matmuhsite.business.constants.CmsMessages;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tools.jackson.databind.JsonNode;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdatePageRequestDto {

    @NotBlank(message = CmsMessages.SLUG_NOT_BLANK)
    private String slug;

    @NotNull(message = CmsMessages.BLOCKS_NOT_NULL)
    @Valid
    private List<BlockUpdateDto> blocks;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BlockUpdateDto {

        @NotBlank(message = CmsMessages.BLOCK_PATH_NOT_BLANK)
        private String blockPath;

        @NotNull(message = CmsMessages.VALUE_NOT_NULL)
        private JsonNode value;

        private Integer version;
    }
}
