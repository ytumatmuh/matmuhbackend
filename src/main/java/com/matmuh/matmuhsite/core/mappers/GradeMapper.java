package com.matmuh.matmuhsite.core.mappers;

import com.matmuh.matmuhsite.core.dtos.examStatistic.response.ExamStatisticDto;
import com.matmuh.matmuhsite.core.dtos.gradeDistribution.response.GradeDistributionDto;
import com.matmuh.matmuhsite.core.dtos.gradeDistribution.response.GradeResultDto;
import com.matmuh.matmuhsite.entities.ExamStatistic;
import com.matmuh.matmuhsite.entities.GradeDistribution;
import com.matmuh.matmuhsite.entities.GradeResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GradeMapper {

    GradeDistributionDto toGradeDistributionDto(GradeDistribution gradeDistribution);

    GradeResultDto toGradeResultDto(GradeResult gradeResult);

    @Mapping(target = "absentStudentCount", expression = "java(examStatistic.getTotalStudentCount() - examStatistic.getAttendedStudentCount())")
    ExamStatisticDto toExamStatisticDto(ExamStatistic examStatistic);

}
