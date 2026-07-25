package com.matmuh.matmuhsite.core.dtos.lectureNote.request;

import com.matmuh.matmuhsite.business.constants.LectureNoteMessages;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LectureNoteApproveRequestDto {

    @NotNull(message = LectureNoteMessages.APPROVED_NOT_NULL)
    private Boolean approved;
}
