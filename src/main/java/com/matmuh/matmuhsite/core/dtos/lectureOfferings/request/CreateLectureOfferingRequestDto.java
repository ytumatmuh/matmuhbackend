package com.matmuh.matmuhsite.core.dtos.lectureOfferings.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Getter
@Service
@AllArgsConstructor
@NoArgsConstructor
public class CreateLectureOfferingRequestDto {

    private UUID lectureId;

    private UUID instructorId;

    private String academicYear;

    private Integer groupNumber;

}
