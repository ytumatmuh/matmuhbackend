package com.matmuh.matmuhsite.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "lecture_offerings")
public class LectureOffering extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "academic_year")
    private String academicYear;

    @Column(name = "group_number")
    private int groupNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    private Instructor instructor;

    @OneToMany(mappedBy = "lectureOffering", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<GradeDistribution> gradeDistributions = new HashSet<>();


}
