package com.matmuh.matmuhsite.entities.dtos;

import com.matmuh.matmuhsite.entities.AnnouncementLink;
import com.matmuh.matmuhsite.entities.Image;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseAnnouncementDto {

    private int id;

    private String title;

    private Date publishDate;

    private String content;

    private String imageUrl;

    private List<AnnouncementLink> links;

}
