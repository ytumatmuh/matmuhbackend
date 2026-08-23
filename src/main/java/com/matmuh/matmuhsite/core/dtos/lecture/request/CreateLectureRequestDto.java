package com.matmuh.matmuhsite.core.dtos.lecture.request;

import com.matmuh.matmuhsite.business.constants.LectureMessages;
import com.matmuh.matmuhsite.core.validation.NullOrNotBlank;
import com.matmuh.matmuhsite.core.dtos.lecture.response.SyllabusWeekDto;
import com.matmuh.matmuhsite.entities.DegreeLevel;
import com.matmuh.matmuhsite.entities.LectureCategory;
import com.matmuh.matmuhsite.entities.LectureType;
import com.matmuh.matmuhsite.entities.Semester;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateLectureRequestDto {

    @NotBlank(message = LectureMessages.LECTURE_NAME_CANNOT_BE_BLANK)
    private String name;

    private String code;

    @NullOrNotBlank(message = LectureMessages.SLUG_NOT_BLANK_IF_PRESENT)
    private String slug;

    private String about;

    private String gradingPolicy;

    private String resources;

    private String language;

    private Integer term;

    private Semester semester;

    private LectureType type;

    private LectureCategory category;

    private List<SyllabusWeekDto> syllabus;

    @Min(value = 0, message = LectureMessages.WEIGHT_MIN)
    @Max(value = 100, message = LectureMessages.WEIGHT_MAX)
    private Integer midtermWeight;

    @Min(value = 0, message = LectureMessages.WEIGHT_MIN)
    @Max(value = 100, message = LectureMessages.WEIGHT_MAX)
    private Integer finalWeight;

    @Min(value = 0, message = LectureMessages.LECTURE_HOURS_MIN)
    private Integer theoryHours;

    @Min(value = 0, message = LectureMessages.LECTURE_HOURS_MIN)
    private Integer practiceHours;

    @Min(value = 0, message = LectureMessages.LECTURE_HOURS_MIN)
    private Integer labHours;

    private Set<DegreeLevel> degreeLevels;

    private Integer weeklyHours;

    private Integer localCredit;

    private Integer ects;

    private String bolognaLink;

    private String notesLink;

}
