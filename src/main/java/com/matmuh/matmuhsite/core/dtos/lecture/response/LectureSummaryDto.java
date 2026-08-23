package com.matmuh.matmuhsite.core.dtos.lecture.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LectureSummaryDto {

    private UUID id;

    private String code;

    private String name;

    private String slug;
}
