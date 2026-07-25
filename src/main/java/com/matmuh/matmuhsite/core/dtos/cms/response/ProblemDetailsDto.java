package com.matmuh.matmuhsite.core.dtos.cms.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProblemDetailsDto {
    private String type;
    private String title;
    private int status;
    private String detail;
    private String instance;
}
