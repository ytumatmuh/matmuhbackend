package com.matmuh.matmuhsite.core.dtos.lecture.response;

import com.matmuh.matmuhsite.core.dtos.instructor.response.InstructorDto;
import com.matmuh.matmuhsite.entities.Semester;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LectureDto {

    private UUID id;

    private String name;

    private String code;

    private String language;

    private String about;

    private String gradingPolicy;

    private String resources;

    private String term;

    private Semester semester;

    private Integer weeklyHours;

    private Integer localCredit;

    private Integer ects;

    private String bolognaLink;;

    private String notesLink;

    private List<InstructorDto> instructors = new ArrayList<>();

}
