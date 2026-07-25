package com.matmuh.matmuhsite.core.dtos.cms.request;

import tools.jackson.databind.JsonNode;
import com.matmuh.matmuhsite.business.constants.CmsMessages;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateCollectionItemRequestDto {

    @NotNull(message = CmsMessages.DATA_NOT_NULL)
    private JsonNode data;
}