package com.matmuh.matmuhsite.entities.dtos;

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
    private String name;

    private String content;


}
