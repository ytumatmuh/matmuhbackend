package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.dtos.enrollment.request.EnrollRequestDto;
import com.matmuh.matmuhsite.core.dtos.enrollment.response.EnrollmentDto;

import java.util.List;
import java.util.UUID;

public interface EnrollmentService {

    EnrollmentDto enroll(String email, EnrollRequestDto request);

    List<EnrollmentDto> getMyEnrollments(String email, String academicYear);

    void unenroll(String email, UUID lectureOfferingId);
}
