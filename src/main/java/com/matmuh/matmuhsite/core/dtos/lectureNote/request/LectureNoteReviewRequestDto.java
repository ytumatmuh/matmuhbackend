package com.matmuh.matmuhsite.core.dtos.lectureNote.request;

import com.matmuh.matmuhsite.business.constants.LectureNoteMessages;
import com.matmuh.matmuhsite.entities.NoteReviewStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LectureNoteReviewRequestDto {

    @NotNull(message = LectureNoteMessages.REVIEW_STATUS_REQUIRED)
    private NoteReviewStatus status;
}
