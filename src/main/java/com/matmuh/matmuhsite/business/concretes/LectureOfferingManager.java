package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.InstructorService;
import com.matmuh.matmuhsite.business.abstracts.LectureOfferingService;
import com.matmuh.matmuhsite.business.abstracts.LectureService;
import com.matmuh.matmuhsite.business.constants.LectureOfferingMessages;
import com.matmuh.matmuhsite.core.dtos.examStatistic.request.SaveExamStatisticRequestDto;
import com.matmuh.matmuhsite.core.dtos.gradeDistribution.request.SaveGradeResultRequestDto;
import com.matmuh.matmuhsite.core.dtos.lectureOfferings.request.CreateLectureOfferingRequestDto;
import com.matmuh.matmuhsite.core.dtos.lectureOfferings.request.UpdateLectureOfferingRequestDto;
import com.matmuh.matmuhsite.core.dtos.lectureOfferings.response.LectureOfferingDto;
import com.matmuh.matmuhsite.core.exceptions.BusinessRuleException;
import com.matmuh.matmuhsite.core.exceptions.ResourceNotFoundException;
import com.matmuh.matmuhsite.core.mappers.LectureOfferingMapper;
import com.matmuh.matmuhsite.dataAccess.abstracts.LectureOfferingDao;
import com.matmuh.matmuhsite.entities.ExamPeriod;
import com.matmuh.matmuhsite.entities.ExamStatistic;
import com.matmuh.matmuhsite.entities.ExamType;
import com.matmuh.matmuhsite.entities.GradeDistribution;
import com.matmuh.matmuhsite.entities.GradeResult;
import com.matmuh.matmuhsite.entities.LectureOffering;
import com.matmuh.matmuhsite.entities.Semester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LectureOfferingManager implements LectureOfferingService {

    private final Logger logger = LoggerFactory.getLogger(LectureOfferingManager.class);
    private final LectureOfferingDao lectureOfferingDao;
    private final LectureService lectureService;
    private final InstructorService instructorService;
    private final LectureOfferingMapper lectureOfferingMapper;

    public LectureOfferingManager(LectureOfferingDao lectureOfferingDao, LectureService lectureService, InstructorService instructorService, LectureOfferingMapper lectureOfferingMapper) {
        this.lectureOfferingDao = lectureOfferingDao;
        this.lectureService = lectureService;
        this.instructorService = instructorService;
        this.lectureOfferingMapper = lectureOfferingMapper;
    }

    @Override
    @Transactional
    public LectureOfferingDto createOffering(UUID lectureId, CreateLectureOfferingRequestDto request) {
        logger.info("Creating lecture offering for lecture ID: {}", lectureId);

        var lecture = lectureService.getLectureById(lectureId);
        var instructor = instructorService.getInstructorById(request.getInstructorId());

        LectureOffering lectureOffering = new LectureOffering();
        lectureOffering.setLecture(lectureService.getLectureReferenceById(lecture.getId()));
        lectureOffering.setInstructor(instructorService.getInstructorReferenceById(instructor.getId()));
        lectureOffering.setAcademicYear(request.getAcademicYear());
        lectureOffering.setSemester(request.getSemester());
        lectureOffering.setGroupNumber(request.getGroupNumber() == null ? 1 : request.getGroupNumber());
        lectureOffering.setLanguage(request.getLanguage());

        LectureOffering savedOffering = lectureOfferingDao.save(lectureOffering);
        logger.info("Lecture offering created with ID: {}", savedOffering.getId());

        return lectureOfferingMapper.toLectureOfferingDto(savedOffering);
    }

    @Override
    @Transactional
    public LectureOfferingDto updateOffering(UUID offeringId, UpdateLectureOfferingRequestDto request) {
        logger.info("Updating lecture offering: {}", offeringId);

        LectureOffering offering = findOffering(offeringId);

        if (request.getInstructorId() != null) {
            var instructor = instructorService.getInstructorById(request.getInstructorId());
            offering.setInstructor(instructorService.getInstructorReferenceById(instructor.getId()));
        }
        if (request.getAcademicYear() != null) {
            offering.setAcademicYear(request.getAcademicYear());
        }
        if (request.getSemester() != null) {
            offering.setSemester(request.getSemester());
        }
        if (request.getGroupNumber() != null) {
            offering.setGroupNumber(request.getGroupNumber());
        if (request.getLanguage() != null) {
            offering.setLanguage(request.getLanguage());
        }
        }

        return lectureOfferingMapper.toLectureOfferingDto(lectureOfferingDao.save(offering));
    }

    @Override
    @Transactional
    public void deleteOffering(UUID offeringId) {
        logger.info("Deleting lecture offering: {}", offeringId);
        lectureOfferingDao.delete(findOffering(offeringId));
    }

    @Override
    @Transactional
    public LectureOfferingDto saveGradeResult(UUID offeringId, ExamPeriod examPeriod, SaveGradeResultRequestDto request) {
        logger.info("Saving {} grade result for offering: {}", examPeriod, offeringId);

        LectureOffering offering = findOffering(offeringId);

        GradeResult gradeResult = offering.getGradeResults().stream()
                .filter(result -> result.getExamPeriod() == examPeriod)
                .findFirst()
                .orElseGet(() -> {
                    GradeResult created = new GradeResult();
                    created.setLectureOffering(offering);
                    created.setExamPeriod(examPeriod);
                    offering.getGradeResults().add(created);
                    return created;
                });

        gradeResult.setEvaluationMethod(request.getEvaluationMethod());
        gradeResult.setResultStatus(request.getResultStatus());
        gradeResult.setResultDate(request.getResultDate());
        gradeResult.setExamCurriculumName(request.getExamCurriculumName());
        gradeResult.setParticipantCount(request.getParticipantCount());
        gradeResult.setClassAverage(request.getClassAverage());
        gradeResult.setClassAverageParticipantCount(request.getClassAverageParticipantCount());
        gradeResult.setStandardDeviation(request.getStandardDeviation());
        gradeResult.setClassLevel(request.getClassLevel());
        gradeResult.setRangesChanged(request.isRangesChanged());

        gradeResult.getGradeDistributions().clear();
        request.getGrades().forEach(gradeDetail -> {
            if (gradeDetail.getMinScore().compareTo(gradeDetail.getMaxScore()) > 0) {
                throw new BusinessRuleException(LectureOfferingMessages.GRADE_RANGE_INVALID);
            }
            GradeDistribution grade = new GradeDistribution();
            grade.setGradeResult(gradeResult);
            grade.setLetterGrade(gradeDetail.getLetterGrade());
            grade.setMinScore(gradeDetail.getMinScore());
            grade.setMaxScore(gradeDetail.getMaxScore());
            grade.setStudentCount(gradeDetail.getStudentCount());
            gradeResult.getGradeDistributions().add(grade);
        });

        LectureOffering savedOffering = lectureOfferingDao.save(offering);
        logger.info("Grade result saved for offering: {}", savedOffering.getId());

        return lectureOfferingMapper.toLectureOfferingDto(savedOffering);
    }

    @Override
    @Transactional
    public void deleteGradeResult(UUID offeringId, ExamPeriod examPeriod) {
        logger.info("Deleting {} grade result for offering: {}", examPeriod, offeringId);

        LectureOffering offering = findOffering(offeringId);

        var removed = offering.getGradeResults().removeIf(result -> result.getExamPeriod() == examPeriod);
        if (!removed) {
            throw new ResourceNotFoundException(LectureOfferingMessages.GRADE_RESULT_NOT_FOUND);
        }

        lectureOfferingDao.save(offering);
    }

    @Override
    @Transactional
    public LectureOfferingDto saveExamStatistic(UUID offeringId, ExamType examType, SaveExamStatisticRequestDto request) {
        logger.info("Saving {} exam statistic for offering: {}", examType, offeringId);

        if (request.getAttendedStudentCount() > request.getTotalStudentCount()) {
            throw new BusinessRuleException(LectureOfferingMessages.EXAM_ATTENDED_EXCEEDS_TOTAL);
        }

        LectureOffering offering = findOffering(offeringId);

        ExamStatistic statistic = offering.getExamStatistics().stream()
                .filter(examStatistic -> examStatistic.getExamType() == examType)
                .findFirst()
                .orElseGet(() -> {
                    ExamStatistic created = new ExamStatistic();
                    created.setLectureOffering(offering);
                    created.setExamType(examType);
                    offering.getExamStatistics().add(created);
                    return created;
                });

        statistic.setWeightPercent(request.getWeightPercent());
        statistic.setAnnouncedAt(request.getAnnouncedAt());
        statistic.setTotalStudentCount(request.getTotalStudentCount());
        statistic.setAttendedStudentCount(request.getAttendedStudentCount());
        statistic.setFailedByAbsenceCount(request.getFailedByAbsenceCount() == null ? 0 : request.getFailedByAbsenceCount());
        statistic.setAverageScore(request.getAverageScore());

        LectureOffering savedOffering = lectureOfferingDao.save(offering);
        logger.info("Exam statistic saved for offering: {}", savedOffering.getId());

        return lectureOfferingMapper.toLectureOfferingDto(savedOffering);
    }

    @Override
    @Transactional
    public void deleteExamStatistic(UUID offeringId, ExamType examType) {
        logger.info("Deleting {} exam statistic for offering: {}", examType, offeringId);

        LectureOffering offering = findOffering(offeringId);

        var removed = offering.getExamStatistics().removeIf(statistic -> statistic.getExamType() == examType);
        if (!removed) {
            throw new ResourceNotFoundException(LectureOfferingMessages.EXAM_STATISTIC_NOT_FOUND);
        }

        lectureOfferingDao.save(offering);
    }

    @Override
    @Transactional(readOnly = true)
    public LectureOfferingDto getOfferingById(UUID offeringId) {
        return lectureOfferingMapper.toLectureOfferingDto(findOffering(offeringId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LectureOfferingDto> getOfferingsByLecture(UUID lectureId) {
        lectureService.getLectureById(lectureId);

        return lectureOfferingDao.findByLectureId(lectureId).stream()
                .map(lectureOfferingMapper::toLectureOfferingDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LectureOfferingDto> getOfferingsByInstructor(UUID instructorId, String academicYear, Semester semester) {
        instructorService.getInstructorById(instructorId);

        return lectureOfferingDao.findByInstructor(instructorId, academicYear, semester).stream()
                .map(lectureOfferingMapper::toLectureOfferingDto)
                .collect(Collectors.toList());
    }

    @Override
    public LectureOffering getOfferingReferenceById(UUID offeringId) {
        return lectureOfferingDao.getReferenceById(offeringId);
    }

    private LectureOffering findOffering(UUID offeringId) {
        return lectureOfferingDao.findWithDetailsById(offeringId).orElseThrow(() -> {
            logger.error("Lecture offering not found with ID: {}", offeringId);
            return new ResourceNotFoundException(LectureOfferingMessages.OFFERING_NOT_FOUND);
        });
    }
}
