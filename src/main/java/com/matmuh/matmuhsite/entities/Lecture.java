package com.matmuh.matmuhsite.entities;

import jakarta.persistence.*;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "lectures")
public class Lecture extends BaseEntity{


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "language")
    private String language;

    @Column(name = "code")
    private String code;

    @Column(name = "about")
    private String about;

    @Column(name = "grading_policy")
    private String gradingPolicy;

    @Column(name = "resources")
    private String resources;

    @Column(name = "term")
    private int term;

    @Enumerated(EnumType.STRING)
    @Column(name = "semester")
    private Semester semester;

    @Column(name = "weekly_hours")
    private int weeklyHours;

    @Column(name = "local_credit")
    private int localCredit;

    @Column(name = "ects")
    private int ects;

    @Column(name = "bologna_link")
    private String bolognaLink;

    @Column(name = "notes_link")
    private String notesLink;

    @OneToMany(mappedBy = "lecture", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<LectureNote> lectureNotes = new ArrayList<>();

    @OneToMany(mappedBy = "lecture", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<LectureOffering> offerings = new HashSet<>();



}
