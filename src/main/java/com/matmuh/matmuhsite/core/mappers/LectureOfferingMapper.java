package com.matmuh.matmuhsite.core.mappers;

import com.matmuh.matmuhsite.core.dtos.lectureOfferings.response.LectureOfferingDto;
import com.matmuh.matmuhsite.core.dtos.lectureOfferings.response.OfferingStatisticsDto;
import com.matmuh.matmuhsite.entities.ExamPeriod;
import com.matmuh.matmuhsite.entities.LectureOffering;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {StaffMapper.class, GradeMapper.class})
public abstract class LectureOfferingMapper {

    @Autowired
    private GradeMapper gradeMapper;

    @Mapping(target = "finalResult", ignore = true)
    @Mapping(target = "butResult", ignore = true)
    public abstract OfferingStatisticsDto offeringStatisticsDto(LectureOffering lectureOffering);

    @AfterMapping
    protected void splitResultsByPeriod(@MappingTarget OfferingStatisticsDto dto, LectureOffering entity){
        if (entity.getGradeResults() != null){
            entity.getGradeResults().forEach(gradeResult -> {
                if (ExamPeriod.NORMAL.equals(gradeResult.getExamPeriod())) {
                    dto.setFinalResult(gradeMapper.toGradeResultDto(gradeResult));
                } else if (ExamPeriod.BUT.equals(gradeResult.getExamPeriod())) {
                    dto.setButResult(gradeMapper.toGradeResultDto(gradeResult));
                }
            });
        }
    }

    @Mapping(target = "instructorName", ignore = true)
    public abstract LectureOfferingDto toLectureOfferingDto(LectureOffering savedOffering);


    @AfterMapping
    protected void resolveInstructorName(@MappingTarget LectureOfferingDto dto, LectureOffering entity) {
        var staff = entity.getStaff();
        if (staff != null) {
            dto.setInstructorName((staff.getFirstName() + " " + staff.getLastName()).trim());
            return;
        }
        dto.setInstructorName(entity.getInstructorRawName());
    }
}
