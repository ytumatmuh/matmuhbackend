package com.matmuh.matmuhsite.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import java.util.Date;
@Data
@RequiredArgsConstructor
public class Project {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private String description;
    private String context;
    private Date date;
}
