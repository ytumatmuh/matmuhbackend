package com.matmuh.matmuhsite.core.dtos.lecture.response;

import com.matmuh.matmuhsite.core.dtos.staff.response.StaffDto;
import com.matmuh.matmuhsite.entities.DegreeLevel;
import com.matmuh.matmuhsite.entities.LectureCategory;
import com.matmuh.matmuhsite.entities.LectureType;
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
public class LectureDto {

    private UUID id;

    private String name;

    private String code;

    private String slug;

    private String language;

    private String about;

    private String gradingPolicy;

    private String resources;

    private Integer term;

    private Semester semester;

    private LectureType type;

    private LectureCategory category;

    private List<SyllabusWeekDto> syllabus = new ArrayList<>();

    private Integer midtermWeight;

    private Integer finalWeight;

    private Integer theoryHours;

    private Integer practiceHours;

    private Integer labHours;

    private Set<DegreeLevel> degreeLevels = new LinkedHashSet<>();

    private Integer weeklyHours;

    private Integer localCredit;

    private Integer ects;

    private String bolognaLink;;

    private String notesLink;

    private List<StaffDto> staff = new ArrayList<>();


    private long noteCount;

    private long statisticsTermCount;

    private long electiveGroupCount;

}
