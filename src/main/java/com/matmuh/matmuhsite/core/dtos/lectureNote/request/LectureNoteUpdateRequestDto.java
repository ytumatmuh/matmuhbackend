package com.matmuh.matmuhsite.core.dtos.lectureNote.request;

import com.matmuh.matmuhsite.entities.NoteReviewStatus;
import com.matmuh.matmuhsite.entities.NoteType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LectureNoteUpdateRequestDto {

    private NoteReviewStatus status;

    private NoteType type;

    public boolean isEmpty() {
        return status == null && type == null;
    }
}
