package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.dtos.gradeDistribution.request.GradeSubmissionRequestDto;
import com.matmuh.matmuhsite.core.dtos.lectureOfferings.request.CreateLectureOfferingRequestDto;
import com.matmuh.matmuhsite.core.dtos.lectureOfferings.response.LectureOfferingDto;

import java.util.UUID;

public interface LectureOfferingService {

    LectureOfferingDto createOffering(CreateLectureOfferingRequestDto createOfferingRequestDto);


    LectureOfferingDto addGradesToOffering(UUID offeringId, GradeSubmissionRequestDto gradeSubmissionRequestDto);

}
