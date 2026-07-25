package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.dtos.common.PageDto;
import com.matmuh.matmuhsite.core.dtos.instructor.request.CreateInstructorRequestDto;
import org.springframework.data.domain.Pageable;
import com.matmuh.matmuhsite.core.dtos.instructor.request.UpdateInstructorRequestDto;
import com.matmuh.matmuhsite.core.dtos.instructor.response.InstructorDto;
import com.matmuh.matmuhsite.entities.Instructor;

import java.util.List;
import java.util.UUID;

public interface InstructorService {

    InstructorDto getInstructorById(UUID id);

    Instructor getInstructorReferenceById(UUID id);

    InstructorDto createInstructor(CreateInstructorRequestDto createInstructorRequestDto);


    InstructorDto updateInstructor(UUID id, UpdateInstructorRequestDto updateInstructorRequestDto);

    void deleteInstructor(UUID id);

    PageDto<InstructorDto> getInstructors(String search, Pageable pageable);

}
