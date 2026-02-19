package com.matmuh.matmuhsite.core.dtos.lectureNote.response;

import com.matmuh.matmuhsite.core.dtos.file.response.FileDto;
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

    private boolean isApproved;

    private SimpleUserDto approvedBy;

    private FileDto file;

}
