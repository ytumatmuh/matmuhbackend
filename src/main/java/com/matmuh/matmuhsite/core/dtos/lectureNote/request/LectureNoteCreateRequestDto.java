package com.matmuh.matmuhsite.core.dtos.lectureNote.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LectureNoteCreateRequestDto {

    private String title;

    private String description;

    private UUID lectureOfferingId;

}
