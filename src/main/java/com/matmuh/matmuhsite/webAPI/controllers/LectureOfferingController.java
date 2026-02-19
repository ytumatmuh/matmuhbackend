package com.matmuh.matmuhsite.webAPI.controllers;

import com.matmuh.matmuhsite.business.abstracts.LectureOfferingService;
import com.matmuh.matmuhsite.business.constants.LectureOfferingMessages;
import com.matmuh.matmuhsite.core.dtos.gradeDistribution.request.GradeSubmissionRequestDto;
import com.matmuh.matmuhsite.core.dtos.lectureOfferings.request.CreateLectureOfferingRequestDto;
import com.matmuh.matmuhsite.core.dtos.lectureOfferings.response.LectureOfferingDto;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.SuccessDataResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/lecture-offerings")
public class LectureOfferingController {

    private final LectureOfferingService lectureOfferingService;


    public LectureOfferingController(LectureOfferingService lectureOfferingService) {
        this.lectureOfferingService = lectureOfferingService;
    }


    @PostMapping
    public ResponseEntity<DataResult<LectureOfferingDto>> createOffering(@Valid @RequestBody CreateLectureOfferingRequestDto requestDto){

        var createdOffering = lectureOfferingService.createOffering(requestDto);



        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessDataResult<>(
                                createdOffering,
                                LectureOfferingMessages.OFFERING_CREATED_SUCCESSFULLY,
                                HttpStatus.CREATED
                        ));

    }


    @PostMapping("/{lectureOfferingId}/grades")
    public ResponseEntity<DataResult<LectureOfferingDto>> addGradesToOffering(@Valid @RequestBody GradeSubmissionRequestDto requestDto, @PathVariable UUID lectureOfferingId){


        var updatedOffering = lectureOfferingService.addGradesToOffering(lectureOfferingId, requestDto);

        String periodName = requestDto.getExamPeriod().name().equals("NORMAL") ? "Final" : "Bütünleme";

        return ResponseEntity.ok(
                new SuccessDataResult<>(
                        updatedOffering,
                        String.format(LectureOfferingMessages.GRADES_ADDED_SUCCESSFULLY, periodName),
                        HttpStatus.OK
                )
        );


    }
}
