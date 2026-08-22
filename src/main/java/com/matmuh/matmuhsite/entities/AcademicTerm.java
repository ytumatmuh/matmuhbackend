package com.matmuh.matmuhsite.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "academic_terms",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_academic_term",
                columnNames = {"academic_year", "semester"}
        )
)
public class AcademicTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "academic_year", length = 20, nullable = false)
    private String academicYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "semester", length = 20, nullable = false)
    private Semester semester;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
}
