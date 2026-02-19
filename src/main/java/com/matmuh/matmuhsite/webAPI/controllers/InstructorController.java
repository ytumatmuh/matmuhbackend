package com.matmuh.matmuhsite.webAPI.controllers;

import com.matmuh.matmuhsite.business.abstracts.InstructorService;
import com.matmuh.matmuhsite.business.constants.InstructorMessages;
import com.matmuh.matmuhsite.core.dtos.instructor.request.CreateInstructorRequestDto;
import com.matmuh.matmuhsite.core.dtos.instructor.response.InstructorDto;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.SuccessDataResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/instructors")
public class InstructorController {

    private final InstructorService instructorService;

    public InstructorController(InstructorService instructorService) {
        this.instructorService = instructorService;
    }


    @PostMapping
    public ResponseEntity<DataResult<InstructorDto>> createInstructor(@Valid @RequestBody CreateInstructorRequestDto requestDto) {

        var createdInstructor = instructorService.createInstructor(requestDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessDataResult<>(
                        createdInstructor,
                        InstructorMessages.INSTRUCTOR_CREATED_SUCCESSFULLY,
                        HttpStatus.CREATED
                ));
    }

    @GetMapping
    public ResponseEntity<DataResult<List<InstructorDto>>> getAllInstructors() {

        var instructors = instructorService.getAllInstructors();

        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessDataResult<>(
                        instructors,
                        InstructorMessages.INSTRUCTORS_FETCHED_SUCCESSFULLY,
                        HttpStatus.OK
                ));
    }


}
