package com.matmuh.matmuhsite.core.dtos.lecture.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SyllabusWeekDto {

    private Integer week;

    private String topic;
}
