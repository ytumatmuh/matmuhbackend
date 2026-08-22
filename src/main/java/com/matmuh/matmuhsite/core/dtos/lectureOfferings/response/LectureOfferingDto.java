package com.matmuh.matmuhsite.core.dtos.lectureOfferings.response;

import com.matmuh.matmuhsite.core.dtos.examStatistic.response.ExamStatisticDto;
import com.matmuh.matmuhsite.core.dtos.gradeDistribution.response.GradeResultDto;
import com.matmuh.matmuhsite.core.dtos.staff.response.StaffDto;
import com.matmuh.matmuhsite.core.dtos.lecture.response.LectureDto;
import com.matmuh.matmuhsite.entities.InstructionLanguage;
import com.matmuh.matmuhsite.entities.Semester;
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

    private Semester semester;

    private int groupNumber;

    private InstructionLanguage language;

    private LectureDto lecture;

    private StaffDto staff;

    private List<GradeResultDto> gradeResults;

    private List<ExamStatisticDto> examStatistics;

}
