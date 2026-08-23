package com.matmuh.matmuhsite.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SyllabusWeek {

    @Column(name = "week", nullable = false)
    private Integer week;

    @Column(name = "topic", nullable = false, length = 1000)
    private String topic;
}
