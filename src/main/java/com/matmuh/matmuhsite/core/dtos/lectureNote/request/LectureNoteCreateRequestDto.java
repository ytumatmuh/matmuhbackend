package com.matmuh.matmuhsite.core.dtos.lectureNote.request;

import com.matmuh.matmuhsite.entities.NoteType;

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

    private NoteType type;

    private UUID lectureOfferingId;

}
