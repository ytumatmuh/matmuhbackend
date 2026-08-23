package com.matmuh.matmuhsite.core.dtos.cms.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProblemDetailsDto {
    private String type;
    private String title;
    private int status;
    private String detail;
    private String instance;

    private String reason;
    private Integer version;
    private String conflictingSlug;

    public ProblemDetailsDto(String type, String title, int status, String detail, String instance) {
        this.type = type;
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.instance = instance;
    }
}
