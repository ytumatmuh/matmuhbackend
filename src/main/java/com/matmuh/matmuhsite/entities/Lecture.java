package com.matmuh.matmuhsite.entities;

import jakarta.persistence.*;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "lectures")
public class Lecture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "lecture_code")
    private String lectureCode;

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
