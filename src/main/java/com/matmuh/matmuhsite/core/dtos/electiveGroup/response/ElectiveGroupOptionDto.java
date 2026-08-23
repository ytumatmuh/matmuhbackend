package com.matmuh.matmuhsite.core.dtos.electiveGroup.response;

import com.matmuh.matmuhsite.entities.DegreeLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ElectiveGroupOptionDto {

    private UUID id;

    private String code;

    private String name;

    private String slug;

    private String language;

    private Integer weeklyHours;

    private Integer localCredit;

    private Integer ects;

    private Set<DegreeLevel> degreeLevels = new LinkedHashSet<>();
}
