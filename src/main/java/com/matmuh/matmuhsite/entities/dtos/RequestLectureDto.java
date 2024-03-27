package com.matmuh.matmuhsite.entities.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RequestLectureDto {
    private String name;
    private String lectureCode;
    private int term;
    private int count;
    private int credit;
    private String syllabusLink;
    private String notesLink;

}
