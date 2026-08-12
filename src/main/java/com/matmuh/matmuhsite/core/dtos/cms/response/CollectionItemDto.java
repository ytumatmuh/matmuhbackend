package com.matmuh.matmuhsite.core.dtos.cms.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CollectionItemDto {
    private UUID id;
    private String collectionKey;
    private String slug;
    private JsonNode data;
    private int version;

    private Instant createdAt;
    private Instant updatedAt;

    private boolean canEdit;
    private JsonNode draftData;

    private Boolean isArchived;
    private Instant archivedAt;

    private String locale;
    private UUID translationGroupId;


    private List<TranslationRefDto> translations;

    public CollectionItemDto(UUID id, String collectionKey, String slug, JsonNode data, int version,
                             boolean canEdit, JsonNode draftData) {
        this.id = id;
        this.collectionKey = collectionKey;
        this.slug = slug;
        this.data = data;
        this.version = version;
        this.canEdit = canEdit;
        this.draftData = draftData;
    }
}