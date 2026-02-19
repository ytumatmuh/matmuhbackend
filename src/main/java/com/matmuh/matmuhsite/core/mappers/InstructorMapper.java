package com.matmuh.matmuhsite.core.mappers;

import com.matmuh.matmuhsite.core.dtos.instructor.request.CreateInstructorRequestDto;
import com.matmuh.matmuhsite.core.dtos.instructor.response.InstructorDto;
import com.matmuh.matmuhsite.entities.Instructor;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InstructorMapper {

    InstructorDto toInstructorDto(Instructor instructor);


    Instructor toInstructor(CreateInstructorRequestDto createInstructorRequestDto);

    List<InstructorDto> toInstructorDtoList(List<Instructor> instructors);
}
