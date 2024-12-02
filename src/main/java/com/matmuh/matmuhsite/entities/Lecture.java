package com.matmuh.matmuhsite.entities;

import jakarta.persistence.*;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "lectures")
public class Lecture {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "code")
    private String code;

    @Column(name = "term")
    private int term;

    @Column(name = "count")
    private int count;

    @Column(name = "credit")
    private int credit;

    @Column(name = "syllabus_link")
    private String syllabusLink;

    @Column(name = "notes_link")
    private String notesLink;

    @Column(name = "lecture_term")
    private String lectureTerm;
}
