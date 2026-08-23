package com.matmuh.matmuhsite.core.dtos.cms.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

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
    private String locale;
    private UUID translationGroupId;
    private Instant updatedAt;
    private Boolean isArchived;


    public static VirtualItemDto pending(String collectionKey, JsonNode data, JsonNode draftData,
                                         String locale, UUID translationGroupId, Instant updatedAt) {
        var dto = new VirtualItemDto();
        dto.collectionKey = collectionKey;
        dto.origin = ORIGIN_PENDING;
        dto.data = data;
        dto.canEdit = true;
        dto.draftData = draftData;
        dto.locale = locale;
        dto.translationGroupId = translationGroupId;
        dto.updatedAt = updatedAt;
        return dto;
    }
}
