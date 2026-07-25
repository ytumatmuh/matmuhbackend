package com.matmuh.matmuhsite.core.dtos.lectureOfferings.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AcademicYearOptionsDto {

    private String current;

    private List<String> years;

}
