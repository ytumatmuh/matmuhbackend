package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.dtos.instructor.request.CreateInstructorRequestDto;
import com.matmuh.matmuhsite.core.dtos.instructor.response.InstructorDto;
import com.matmuh.matmuhsite.entities.Instructor;

import java.util.List;
import java.util.UUID;

public interface InstructorService {

    InstructorDto getInstructorById(UUID id);

    Instructor getInstructorReferenceById(UUID id);

    InstructorDto createInstructor(CreateInstructorRequestDto createInstructorRequestDto);


    List<InstructorDto> getAllInstructors();

}
