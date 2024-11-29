package com.matmuh.matmuhsite.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "projects")
public class Project {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "description", length = 8192)
    private String description;

    @Column(name = "date")
    private LocalDateTime date;

    @OneToOne(cascade = CascadeType.ALL)
    private Image image;
}

