package com.matmuh.matmuhsite.webAPI.dtos.announcements;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestAnnouncementDto {

    private int id;

    private String title;

    private String content;


}
