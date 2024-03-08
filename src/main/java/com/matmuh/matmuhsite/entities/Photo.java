package com.matmuh.matmuhsite.entities;

import lombok.Data;

@Re
@Data
public class Photo{
    private int id;
    private String photoUrl;
    private int eventId;
    private int projectId;
    private int announcementId;
}
