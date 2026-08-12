package com.matmuh.matmuhsite.core.dtos.cms.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VirtualItemDto {

    public static final String ORIGIN_PENDING = "pending";
    public static final String ORIGIN_DERIVED = "derived";

    private String collectionKey;
    private String origin;
    private JsonNode data;
    private boolean canEdit;

    private String slug;
    private JsonNode draftData;

    public static VirtualItemDto pending(String collectionKey, String slug, JsonNode data, JsonNode draftData) {
        var dto = new VirtualItemDto();
        dto.collectionKey = collectionKey;
        dto.origin = ORIGIN_PENDING;
        dto.data = data;
        dto.canEdit = true;
        dto.slug = slug;
        dto.draftData = draftData;
        return dto;
    }
}
