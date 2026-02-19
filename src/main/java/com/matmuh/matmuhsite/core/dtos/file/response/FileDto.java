package com.matmuh.matmuhsite.core.dtos.file.response;

import com.matmuh.matmuhsite.core.dtos.user.response.SimpleUserDto;
import com.matmuh.matmuhsite.core.dtos.user.response.UserDto;
import com.matmuh.matmuhsite.entities.File;
import com.matmuh.matmuhsite.entities.User;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileDto {

    private UUID id;

    private String fileName;

    private String fileType;

    private String fileUrl;

    private Long fileSize;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private SimpleUserDto createdBy;

}
