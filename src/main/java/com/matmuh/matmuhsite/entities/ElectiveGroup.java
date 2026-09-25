package com.matmuh.matmuhsite.entities;

import java.math.BigDecimal;

import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE elective_groups SET is_deleted = true WHERE id = ? AND version = ?")
@SQLRestriction("is_deleted = false")
@Entity
@Table(name = "elective_groups")
public class ElectiveGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "name_en")
    private String nameEn;

    @Column(name = "slug")
    private String slug;

    @Version
    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "about", columnDefinition = "TEXT")
    private String about;

    @Column(name = "about_en", columnDefinition = "TEXT")
    private String aboutEn;

    @Column(name = "term")
    private Integer term;

    @Enumerated(EnumType.STRING)
    @Column(name = "semester")
    private Semester semester;

    @ElementCollection(targetClass = DegreeLevel.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "elective_group_degree_levels", joinColumns = @JoinColumn(name = "elective_group_id"))
    @Column(name = "degree_level", nullable = false)
    @Enumerated(EnumType.STRING)
    @BatchSize(size = 50)
    @Builder.Default
    private Set<DegreeLevel> degreeLevels = new LinkedHashSet<>();

    @ElementCollection(targetClass = Program.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "elective_group_programs", joinColumns = @JoinColumn(name = "elective_group_id"))
    @Column(name = "program", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    @BatchSize(size = 50)
    @Builder.Default
    private Set<Program> programs = new LinkedHashSet<>();

    @Column(name = "weekly_hours")
    private Integer weeklyHours;

    @Column(name = "local_credit")
    private Integer localCredit;

    @Column(name = "ects", precision = 4, scale = 1)
    private BigDecimal ects;

    @Column(name = "selection_count", nullable = false)
    @Builder.Default
    private int selectionCount = 1;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "elective_group_options",
            joinColumns = @JoinColumn(name = "elective_group_id"),
            inverseJoinColumns = @JoinColumn(name = "lecture_id"))
    @BatchSize(size = 50)
    @Builder.Default
    private Set<Lecture> options = new LinkedHashSet<>();
}
