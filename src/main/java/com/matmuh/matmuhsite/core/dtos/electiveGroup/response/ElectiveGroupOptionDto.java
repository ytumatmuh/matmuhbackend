package com.matmuh.matmuhsite.core.dtos.electiveGroup.response;

import com.matmuh.matmuhsite.entities.DegreeLevel;
import com.matmuh.matmuhsite.entities.InstructionLanguage;
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

    private String nameEn;

    private String slug;

    private Set<InstructionLanguage> languages = new LinkedHashSet<>();

    private Integer weeklyHours;

    private Integer localCredit;

    private Integer ects;

    private Set<DegreeLevel> degreeLevels = new LinkedHashSet<>();
}
