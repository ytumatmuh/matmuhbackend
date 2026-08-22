package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.EnrollmentService;
import com.matmuh.matmuhsite.business.abstracts.UserService;
import com.matmuh.matmuhsite.business.constants.EnrollmentMessages;
import com.matmuh.matmuhsite.core.dtos.enrollment.request.EnrollRequestDto;
import com.matmuh.matmuhsite.core.dtos.enrollment.response.EnrollmentDto;
import com.matmuh.matmuhsite.core.exceptions.ResourceAlreadyExistsException;
import com.matmuh.matmuhsite.core.exceptions.ResourceNotFoundException;
import com.matmuh.matmuhsite.dataAccess.abstracts.EnrollmentDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.LectureOfferingDao;
import com.matmuh.matmuhsite.entities.Enrollment;
import com.matmuh.matmuhsite.entities.LectureOffering;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class EnrollmentManager implements EnrollmentService {

    private final Logger logger = LoggerFactory.getLogger(EnrollmentManager.class);

    private final EnrollmentDao enrollmentDao;
    private final LectureOfferingDao lectureOfferingDao;
    private final UserService userService;

    public EnrollmentManager(EnrollmentDao enrollmentDao,
                             LectureOfferingDao lectureOfferingDao,
                             UserService userService) {
        this.enrollmentDao = enrollmentDao;
        this.lectureOfferingDao = lectureOfferingDao;
        this.userService = userService;
    }

    @Override
    @Transactional
    public EnrollmentDto enroll(String email, EnrollRequestDto request) {
        var user = userService.getUserEntityByEmail(email);

        if (enrollmentDao.existsByUserIdAndLectureOfferingId(user.getId(), request.getLectureOfferingId())) {
            throw new ResourceAlreadyExistsException(EnrollmentMessages.ALREADY_ENROLLED);
        }

        var offering = lectureOfferingDao.findWithDetailsById(request.getLectureOfferingId())
                .orElseThrow(() -> new ResourceNotFoundException(EnrollmentMessages.OFFERING_NOT_FOUND));

        var saved = enrollmentDao.save(Enrollment.builder()
                .user(user)
                .lectureOffering(offering)
                .build());

        logger.info("User {} enrolled in offering {}", email, offering.getId());
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentDto> getMyEnrollments(String email, String academicYear) {
        var user = userService.getUserEntityByEmail(email);

        return enrollmentDao.findByUserId(user.getId()).stream()
                .filter(enrollment -> academicYear == null || academicYear.isBlank()
                        || academicYear.equals(enrollment.getLectureOffering().getAcademicYear()))
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void unenroll(String email, UUID lectureOfferingId) {
        var user = userService.getUserEntityByEmail(email);

        var enrollment = enrollmentDao.findByUserIdAndLectureOfferingId(user.getId(), lectureOfferingId)
                .orElseThrow(() -> new ResourceNotFoundException(EnrollmentMessages.NOT_ENROLLED));

        enrollmentDao.delete(enrollment);
        logger.info("User {} unenrolled from offering {}", email, lectureOfferingId);
    }

    private EnrollmentDto toDto(Enrollment enrollment) {
        var offering = enrollment.getLectureOffering();
        var lecture = offering.getLecture();

        return new EnrollmentDto(
                enrollment.getId(),
                offering.getId(),
                lecture.getCode(),
                lecture.getName(),
                lecture.getTerm(),
                offering.getAcademicYear(),
                offering.getSemester(),
                offering.getGroupNumber(),
                offering.getLanguage(),
                staffName(offering),
                enrollment.getCreatedAt());
    }

    private String staffName(LectureOffering offering) {
        var staff = offering.getStaff();
        return staff == null ? null : (staff.getFirstName() + " " + staff.getLastName()).trim();
    }
}
