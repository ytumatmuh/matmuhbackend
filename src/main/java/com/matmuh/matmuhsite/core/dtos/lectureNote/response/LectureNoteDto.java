package com.matmuh.matmuhsite.core.dtos.lectureNote.response;

import com.matmuh.matmuhsite.entities.NoteReviewStatus;
import com.matmuh.matmuhsite.entities.NoteType;

import com.matmuh.matmuhsite.core.dtos.file.response.FileDto;
import com.matmuh.matmuhsite.core.dtos.lectureOfferings.response.OfferingSummaryDto;
import com.matmuh.matmuhsite.core.dtos.user.response.SimpleUserDto;
import com.matmuh.matmuhsite.core.dtos.user.response.UserDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LectureNoteDto {

    private UUID id;

    private String title;

    private String description;

    private SimpleUserDto createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer viewCount;

    private NoteType type;

    private NoteReviewStatus status;

    private SimpleUserDto approvedBy;

    private String previewUrl;

    private OfferingSummaryDto offering;

    private FileDto file;

}
