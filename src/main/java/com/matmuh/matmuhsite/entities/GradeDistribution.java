package com.matmuh.matmuhsite.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "grade_distributions")
public class GradeDistribution extends BaseEntity{


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_offering_id")
    private LectureOffering lectureOffering;

    @Enumerated(EnumType.STRING)
    @Column(name = "exam_period")
    private ExamPeriod examPeriod;

    @Column(name = "letter_grade", length = 2)
    private String letterGrade;

    @Column(name = "min_score")
    private int minScore;

    @Column(name = "max_score")
    private int maxScore;

    @Column(name = "student_count")
    private int studentCount;

}
