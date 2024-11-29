package com.matmuh.matmuhsite.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "researches")
public class Research {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "title")
    private String title;

    @Column(name = "description", length = 8192)
    private String description;

}
