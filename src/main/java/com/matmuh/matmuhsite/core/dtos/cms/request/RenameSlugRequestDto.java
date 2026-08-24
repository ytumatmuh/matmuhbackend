package com.matmuh.matmuhsite.core.dtos.cms.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.matmuh.matmuhsite.business.constants.CmsMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RenameSlugRequestDto {

    @NotBlank(message = CmsMessages.SLUG_REQUIRED)
    private String slug;

    @NotNull(message = CmsMessages.VERSION_REQUIRED_FOR_RENAME)
    private Integer version;
}
