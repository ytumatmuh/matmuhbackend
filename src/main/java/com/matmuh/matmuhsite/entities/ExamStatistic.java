package com.matmuh.matmuhsite.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE exam_statistics SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Entity
@Table(name = "exam_statistics")
public class ExamStatistic extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lecture_offering_id", nullable = false)
    private LectureOffering lectureOffering;

    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type", length = 20, nullable = false)
    private ExamType examType;

    @Column(name = "weight_percent")
    private Integer weightPercent;

    @Column(name = "announced_at")
    private LocalDateTime announcedAt;

    @Column(name = "total_student_count", nullable = false)
    private int totalStudentCount;

    @Column(name = "attended_student_count", nullable = false)
    private int attendedStudentCount;

    @Column(name = "failed_by_absence_count", nullable = false)
    private int failedByAbsenceCount;

    @Column(name = "average_score", precision = 5, scale = 2)
    private BigDecimal averageScore;
}
