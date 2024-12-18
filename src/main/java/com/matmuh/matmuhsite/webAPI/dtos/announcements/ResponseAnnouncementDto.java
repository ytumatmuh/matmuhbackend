package com.matmuh.matmuhsite.webAPI.dtos.announcements;

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
public class ResponseAnnouncementDto {

    private UUID id;

    private String title;

    private LocalDateTime publishDate;

    private String content;

    private String coverImageUrl;

    private ResponseUserDto publisher;


}
