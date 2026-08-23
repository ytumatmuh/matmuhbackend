package com.matmuh.matmuhsite.core.dtos.electiveGroup.response;

import com.matmuh.matmuhsite.entities.DegreeLevel;
import com.matmuh.matmuhsite.entities.Semester;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ElectiveGroupDto {

    private UUID id;

    private String code;

    private String name;

    private String slug;

    private String about;

    private Integer term;

    private Semester semester;

    private Set<DegreeLevel> degreeLevels = new LinkedHashSet<>();

    private Integer weeklyHours;

    private Integer localCredit;

    private Integer ects;

    private Integer selectionCount;

    private List<ElectiveGroupOptionDto> options = new ArrayList<>();

    private int optionCount;
}
