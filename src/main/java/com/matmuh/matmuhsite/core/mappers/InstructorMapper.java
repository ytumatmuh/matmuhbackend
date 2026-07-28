package com.matmuh.matmuhsite.core.mappers;

import com.matmuh.matmuhsite.core.dtos.instructor.request.CreateInstructorRequestDto;
import com.matmuh.matmuhsite.core.dtos.instructor.request.UpdateInstructorRequestDto;
import com.matmuh.matmuhsite.core.dtos.instructor.response.InstructorDto;
import com.matmuh.matmuhsite.entities.Instructor;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InstructorMapper {

    InstructorDto toInstructorDto(Instructor instructor);

    Instructor toInstructor(CreateInstructorRequestDto createInstructorRequestDto);

    List<InstructorDto> toInstructorDtoList(List<Instructor> instructors);

    @org.mapstruct.BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @org.mapstruct.Mapping(target = "id", ignore = true)
    @org.mapstruct.Mapping(target = "offerings", ignore = true)
    void updateInstructorFromDto(UpdateInstructorRequestDto dto, @org.mapstruct.MappingTarget Instructor instructor);
}
