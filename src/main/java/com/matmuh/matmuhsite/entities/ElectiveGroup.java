package com.matmuh.matmuhsite.entities;

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

    @Column(name = "slug")
    private String slug;

    @Version
    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "about", columnDefinition = "TEXT")
    private String about;

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

    @Column(name = "weekly_hours")
    private Integer weeklyHours;

    @Column(name = "local_credit")
    private Integer localCredit;

    @Column(name = "ects")
    private Integer ects;

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
