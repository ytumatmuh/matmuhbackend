package com.matmuh.matmuhsite.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExamWeight {

    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type", length = 20, nullable = false)
    private ExamType examType;

    @Column(name = "weight_percent", nullable = false)
    private Integer weightPercent;
}
