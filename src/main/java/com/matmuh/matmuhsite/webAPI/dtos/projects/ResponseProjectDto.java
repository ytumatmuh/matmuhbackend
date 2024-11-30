package com.matmuh.matmuhsite.webAPI.dtos.projects;

import com.matmuh.matmuhsite.webAPI.dtos.users.ResponseUserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseProjectDto {

    private UUID id;

    private String name;

    private String description;

    private LocalDateTime date;

    private String coverImageUrl;

    private ResponseUserDto publisher;
}
