package com.matmuh.matmuhsite.core.dtos.lectureOfferings.response;

import com.matmuh.matmuhsite.entities.Semester;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OfferingSummaryDto {

    private UUID id;

    private String academicYear;

    private Semester semester;

    private Integer groupNumber;

    private String instructorName;
}
