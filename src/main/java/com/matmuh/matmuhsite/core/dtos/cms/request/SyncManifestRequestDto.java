package com.matmuh.matmuhsite.core.dtos.cms.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.matmuh.matmuhsite.business.constants.CmsMessages;
import com.matmuh.matmuhsite.entities.cms.BlockType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SyncManifestRequestDto {

    @NotBlank(message = CmsMessages.SLUG_NOT_BLANK)
    private String slug;

    @NotNull(message = CmsMessages.BLOCKS_NOT_NULL)
    @Valid
    private List<ManifestBlockDto> blocks;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ManifestBlockDto {

        @NotBlank(message = CmsMessages.BLOCK_PATH_NOT_BLANK)
        private String blockPath;

        @NotNull(message = CmsMessages.BLOCK_TYPE_NOT_NULL)
        private BlockType blockType;

        private JsonNode defaultValue;

        // Dil başına tohum: {"tr": ..., "en": ...}. Verilmeyen dil defaultValue'ya düşer.
        // Tek defaultValue her dile aynı (Türkçe) metni yazıyordu ve /en Türkçe kalıyordu.
        private Map<String, JsonNode> defaultValues;

        private Integer sortOrder;

        private JsonNode itemSchema;

        public JsonNode defaultFor(String locale) {
            if (locale != null && defaultValues != null && defaultValues.containsKey(locale)) {
                return defaultValues.get(locale);
            }
            return defaultValue;
        }

        public boolean hasLocaleDefault(String locale) {
            return locale != null && defaultValues != null && defaultValues.get(locale) != null
                    && !defaultValues.get(locale).isNull();
        }
    }
}
