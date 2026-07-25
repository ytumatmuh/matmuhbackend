package com.matmuh.matmuhsite.core.dtos.lecture.request;

import com.matmuh.matmuhsite.business.constants.LectureMessages;
import com.matmuh.matmuhsite.core.validation.NullOrNotBlank;
import com.matmuh.matmuhsite.entities.Semester;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateLectureRequestDto {

    @NullOrNotBlank(message = LectureMessages.LECTURE_NAME_NOT_BLANK_IF_PRESENT)
    private String name;

    @NullOrNotBlank(message = LectureMessages.LECTURE_CODE_NOT_BLANK_IF_PRESENT)
    private String code;

    private String about;

    private String gradingPolicy;

    private String resources;

    private String language;

    @Min(value = 1, message = LectureMessages.LECTURE_TERM_MIN)
    private Integer term;

    private Semester semester;

    @Min(value = 0, message = LectureMessages.LECTURE_HOURS_MIN)
    private Integer weeklyHours;

    @Min(value = 0, message = LectureMessages.LECTURE_CREDIT_MIN)
    private Integer localCredit;

    @Min(value = 0, message = LectureMessages.LECTURE_ECTS_MIN)
    private Integer ects;

    private String bolognaLink;

    private String notesLink;
}
