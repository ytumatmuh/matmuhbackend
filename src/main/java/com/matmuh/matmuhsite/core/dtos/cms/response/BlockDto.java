package com.matmuh.matmuhsite.core.dtos.cms.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.matmuh.matmuhsite.entities.cms.BlockType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tools.jackson.databind.JsonNode;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BlockDto {
    private String blockPath;
    private BlockType blockType;
    private JsonNode value;
    private int sortOrder;
    private int version;

    private JsonNode draftValue;

    public BlockDto(String blockPath, BlockType blockType, JsonNode value, int sortOrder, int version) {
        this(blockPath, blockType, value, sortOrder, version, null);
    }
}
