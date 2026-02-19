package com.matmuh.matmuhsite.core.mappers;

import com.matmuh.matmuhsite.core.dtos.gradeDistribution.response.GradeDistributionDto;
import com.matmuh.matmuhsite.core.dtos.lectureOfferings.response.LectureOfferingDto;
import com.matmuh.matmuhsite.entities.ExamPeriod;
import com.matmuh.matmuhsite.entities.LectureOffering;
import com.matmuh.matmuhsite.core.dtos.lectureOfferings.response.OfferingStatisticsDto;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring", uses = InstructorMapper.class)
public abstract class LectureOfferingMapper {

    @Autowired
    private GradeMapper gradeMapper;


    @Mapping(target = "finalGrades", ignore = true)
    @Mapping(target = "butGrades", ignore = true)
    public abstract OfferingStatisticsDto offeringStatisticsDto(LectureOffering lectureOffering);

    @AfterMapping
    protected void splitGradesByType(@MappingTarget OfferingStatisticsDto dto, LectureOffering entity){


        if (entity.getGradeDistributions() != null){

            List<GradeDistributionDto> finalGrades = entity.getGradeDistributions().stream()
                    .filter(gradeDistribution -> ExamPeriod.NORMAL.equals(gradeDistribution.getExamPeriod()))
                    .map(gradeMapper::toGradeDistributionDto)
                    .toList();

            List<GradeDistributionDto> butGrades = entity.getGradeDistributions().stream()
                    .filter(gradeDistribution -> ExamPeriod.BUT.equals(gradeDistribution.getExamPeriod()))
                    .map(gradeMapper::toGradeDistributionDto)
                    .toList();

            dto.setFinalGrades(finalGrades);
            dto.setButGrades(butGrades);
        }

    }

    public abstract LectureOfferingDto toLectureOfferingDto(LectureOffering savedOffering);

}
