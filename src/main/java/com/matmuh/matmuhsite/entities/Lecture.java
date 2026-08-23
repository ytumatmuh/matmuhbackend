package com.matmuh.matmuhsite.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import jakarta.persistence.Id;
import lombok.*;

import java.util.*;

@Getter
@Setter
@SQLDelete(sql = "UPDATE lectures SET is_deleted = true WHERE id = ? AND version = ?")
@SQLRestriction("is_deleted = false")
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

    @Column(name = "slug")
    private String slug;

    @Version
    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "about")
    private String about;

    @Column(name = "grading_policy")
    private String gradingPolicy;

    @Column(name = "resources")
    private String resources;

    @Column(name = "term")
    private Integer term;

    @ElementCollection(targetClass = DegreeLevel.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "lecture_degree_levels", joinColumns = @JoinColumn(name = "lecture_id"))
    @Column(name = "degree_level", nullable = false)
    @Enumerated(EnumType.STRING)
    @BatchSize(size = 50)
    @Builder.Default
    private Set<DegreeLevel> degreeLevels = new LinkedHashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "semester")
    private Semester semester;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20)
    private LectureType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 32)
    private LectureCategory category;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "lecture_syllabus", joinColumns = @JoinColumn(name = "lecture_id"))
    @OrderBy("week ASC")
    @BatchSize(size = 50)
    @Builder.Default
    private List<SyllabusWeek> syllabus = new ArrayList<>();

    @Column(name = "midterm_weight")
    private Integer midtermWeight;

    @Column(name = "final_weight")
    private Integer finalWeight;

    @Column(name = "theory_hours")
    private Integer theoryHours;

    @Column(name = "practice_hours")
    private Integer practiceHours;

    @Column(name = "lab_hours")
    private Integer labHours;

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
