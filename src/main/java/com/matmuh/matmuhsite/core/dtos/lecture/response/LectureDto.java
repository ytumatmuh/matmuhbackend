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

    private String term;

    private Semester semester;

    private int count;

    private int credit;

    private String bolognaLink;;

    private String notesLink;

    private List<InstructorDto> instructors = new ArrayList<>();

}
