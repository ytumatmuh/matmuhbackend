package com.matmuh.matmuhsite.core.dtos.lecture.request;

import com.matmuh.matmuhsite.business.constants.LectureMessages;
import com.matmuh.matmuhsite.core.validation.NullOrNotBlank;
import com.matmuh.matmuhsite.entities.DegreeLevel;
import com.matmuh.matmuhsite.entities.Semester;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    private Set<DegreeLevel> degreeLevels;

    private Integer weeklyHours;

    private Integer localCredit;

    private Integer ects;

    private String bolognaLink;

    private String notesLink;

}
