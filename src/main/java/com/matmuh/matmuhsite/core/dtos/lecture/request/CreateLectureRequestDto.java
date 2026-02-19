package com.matmuh.matmuhsite.core.dtos.lecture.request;

import com.matmuh.matmuhsite.entities.Semester;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateLectureRequestDto {


    private String name;

    private String code;

    private String term;

    private Semester semester;

    private Integer count;

    private Integer credit;

    private String bolognaLink;

    private String notesLink;

}
