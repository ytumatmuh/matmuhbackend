package com.matmuh.matmuhsite.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

//
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "announcements")
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "title")
    private String title;

    @Column(name = "publish_date")
    private Date publishDate;

    @Column(name = "content", length = 8192)
    private String content;

    @OneToOne(cascade = CascadeType.ALL)
    private Image coverImage;

}
