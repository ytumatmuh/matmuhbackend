package com.matmuh.matmuhsite.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "announcements")
public class Announcement extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "title")
    private String title;

    @Column(name = "publish_date")
    private LocalDateTime publishDate;

    @Column(name = "content", length = 8192)
    private String content;

    @OneToOne(cascade = CascadeType.ALL)
    private Image coverImage;

}
