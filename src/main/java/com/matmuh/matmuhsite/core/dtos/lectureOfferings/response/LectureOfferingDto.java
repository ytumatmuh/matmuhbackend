package com.matmuh.matmuhsite.core.dtos.lectureOfferings.response;

import com.matmuh.matmuhsite.core.dtos.gradeDistribution.response.GradeDistributionDto;
import com.matmuh.matmuhsite.core.dtos.instructor.response.InstructorDto;
import com.matmuh.matmuhsite.core.dtos.lecture.response.LectureDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LectureOfferingDto {

    private UUID id;

    private String academicYear;

    private int groupNumber;

    private LectureDto lecture;

    private InstructorDto instructor;

    private List<GradeDistributionDto> gradeDistributions;

}
