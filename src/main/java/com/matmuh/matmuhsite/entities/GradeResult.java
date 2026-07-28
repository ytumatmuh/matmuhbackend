package com.matmuh.matmuhsite.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE grade_results SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Entity
@Table(name = "grade_results")
public class GradeResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lecture_offering_id", nullable = false)
    private LectureOffering lectureOffering;

    @Enumerated(EnumType.STRING)
    @Column(name = "exam_period", length = 10, nullable = false)
    private ExamPeriod examPeriod;

    @Enumerated(EnumType.STRING)
    @Column(name = "evaluation_method", length = 10)
    private EvaluationMethod evaluationMethod;

    @Column(name = "result_status")
    private String resultStatus;

    @Column(name = "result_date")
    private LocalDate resultDate;

    @Column(name = "exam_curriculum_name")
    private String examCurriculumName;

    @Column(name = "participant_count")
    private Integer participantCount;

    @Column(name = "class_average", precision = 5, scale = 2)
    private BigDecimal classAverage;

    @Column(name = "class_average_participant_count")
    private Integer classAverageParticipantCount;

    @Column(name = "standard_deviation", precision = 6, scale = 2)
    private BigDecimal standardDeviation;

    @Column(name = "class_level")
    private String classLevel;

    @Column(name = "ranges_changed", nullable = false)
    @Builder.Default
    private boolean rangesChanged = false;

    @OneToMany(mappedBy = "gradeResult", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<GradeDistribution> gradeDistributions = new HashSet<>();
}
