package com.matmuh.matmuhsite.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import lombok.*;

import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE lecture_offerings SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Entity
@Table(name = "lecture_offerings")
public class LectureOffering extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "academic_year")
    private String academicYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "semester", length = 10)
    private Semester semester;

    @Column(name = "group_number")
    private int groupNumber;


    @Enumerated(EnumType.STRING)
    @Column(name = "language", length = 20)
    private InstructionLanguage language;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private Staff staff;

    @OneToMany(mappedBy = "lectureOffering", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<GradeResult> gradeResults = new HashSet<>();

    @OneToMany(mappedBy = "lectureOffering", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ExamStatistic> examStatistics = new HashSet<>();

}
