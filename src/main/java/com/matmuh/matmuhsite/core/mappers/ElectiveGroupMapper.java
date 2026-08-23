package com.matmuh.matmuhsite.core.mappers;

import com.matmuh.matmuhsite.core.dtos.electiveGroup.request.CreateElectiveGroupRequestDto;
import com.matmuh.matmuhsite.core.dtos.electiveGroup.request.UpdateElectiveGroupRequestDto;
import com.matmuh.matmuhsite.core.dtos.electiveGroup.response.ElectiveGroupDto;
import com.matmuh.matmuhsite.core.dtos.electiveGroup.response.ElectiveGroupOptionDto;
import com.matmuh.matmuhsite.entities.ElectiveGroup;
import com.matmuh.matmuhsite.entities.Lecture;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ElectiveGroupMapper {

    @Mapping(target = "optionCount", expression = "java(electiveGroup.getOptions() == null ? 0 : electiveGroup.getOptions().size())")
    ElectiveGroupDto toDto(ElectiveGroup electiveGroup);

    List<ElectiveGroupDto> toDtoList(List<ElectiveGroup> electiveGroups);

    ElectiveGroupOptionDto toOptionDto(Lecture lecture);

    List<ElectiveGroupOptionDto> toOptionDtoList(List<Lecture> lectures);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "options", ignore = true)
    @Mapping(target = "slug", ignore = true)
    ElectiveGroup toEntity(CreateElectiveGroupRequestDto requestDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "options", ignore = true)
    @Mapping(target = "slug", ignore = true)
    void updateFromDto(UpdateElectiveGroupRequestDto dto, @MappingTarget ElectiveGroup electiveGroup);
}
