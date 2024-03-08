package com.matmuh.matmuhsite.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Research {
    @GeneratedValue(GenerationType.IDENTITY)
    private int id;
    private String title;
    private String description;
    private String context;
}
