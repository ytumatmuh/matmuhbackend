package com.matmuh.matmuhsite.core.mappers;

import com.matmuh.matmuhsite.core.dtos.gradeDistribution.response.GradeDistributionDto;
import com.matmuh.matmuhsite.entities.GradeDistribution;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GradeMapper {

    GradeDistributionDto toGradeDistributionDto(GradeDistribution gradeDistribution);

}
