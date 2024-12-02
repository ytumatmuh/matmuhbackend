package com.matmuh.matmuhsite.webAPI.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RequestLectureDto {

    private UUID id;

    private String name;

    private String code;

    private int term;

    private int count;

    private int credit;

    private String syllabusLink;

    private String notesLink;

}
