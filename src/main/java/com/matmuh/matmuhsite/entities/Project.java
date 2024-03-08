package com.matmuh.matmuhsite.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "projects")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "context")
    private String context;
    @Column(name = "date")
    private Date date;
}
