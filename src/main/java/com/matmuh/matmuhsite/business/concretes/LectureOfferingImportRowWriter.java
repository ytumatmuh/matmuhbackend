package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.constants.LectureMessages;
import com.matmuh.matmuhsite.business.constants.StaffMessages;
import com.matmuh.matmuhsite.core.dtos.lectureOfferings.request.ImportOfferingsRequestDto;
import com.matmuh.matmuhsite.core.exceptions.BusinessRuleException;
import com.matmuh.matmuhsite.core.exceptions.ResourceNotFoundException;
import com.matmuh.matmuhsite.dataAccess.abstracts.StaffDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.LectureDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.LectureOfferingDao;
import com.matmuh.matmuhsite.entities.ExamStatistic;
import com.matmuh.matmuhsite.entities.GradeDistribution;
import com.matmuh.matmuhsite.entities.GradeResult;
import com.matmuh.matmuhsite.entities.LectureOffering;
import com.matmuh.matmuhsite.business.constants.LectureOfferingMessages;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Component
public class LectureOfferingImportRowWriter {

    public record RowOutcome(UUID offeringId, boolean created) {
    }

    private final LectureDao lectureDao;
    private final StaffDao staffDao;
    private final LectureOfferingDao lectureOfferingDao;

    public LectureOfferingImportRowWriter(LectureDao lectureDao, StaffDao staffDao,
                                          LectureOfferingDao lectureOfferingDao) {
        this.lectureDao = lectureDao;
        this.staffDao = staffDao;
        this.lectureOfferingDao = lectureOfferingDao;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RowOutcome write(ImportOfferingsRequestDto.Row row) {
        var lecture = lectureDao.findByCodeIgnoreCase(row.getLectureCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        LectureMessages.LECTURE_NOT_FOUND + " (" + row.getLectureCode() + ")"));

        var staff = row.getStaffId() == null ? null : staffDao.findById(row.getStaffId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        StaffMessages.STAFF_NOT_FOUND + " (" + row.getStaffId() + ")"));

        if (staff != null) {
            var groups = staff.getGroups();
            if (groups != null && !groups.isEmpty() && groups.stream().noneMatch(com.matmuh.matmuhsite.entities.StaffGroup::canTeach)) {
                throw new com.matmuh.matmuhsite.core.exceptions.BusinessRuleException(StaffMessages.STAFF_CANNOT_TEACH);
            }
        }

        var existing = lectureOfferingDao.findByLectureIdAndAcademicYearAndSemesterAndGroupNumber(
                lecture.getId(), row.getAcademicYear(), row.getSemester(), row.getGroupNumber());

        var created = existing.isEmpty();
        var offering = existing.orElseGet(() -> {
            var fresh = new LectureOffering();
            fresh.setLecture(lecture);
            fresh.setAcademicYear(row.getAcademicYear());
            fresh.setSemester(row.getSemester());
            fresh.setGroupNumber(row.getGroupNumber());
            return fresh;
        });

        offering.setStaff(staff);
        offering.setInstructorRawName(normalize(row.getInstructorRawName()));
        if (row.getLanguage() != null) {
            offering.setLanguage(row.getLanguage());
        }

        applyGradeResults(offering, row.getGradeResults());
        applyExamStatistics(offering, row.getExamStatistics());

        var saved = lectureOfferingDao.save(offering);
        return new RowOutcome(saved.getId(), created);
    }

    private void applyGradeResults(LectureOffering offering,
                                   List<ImportOfferingsRequestDto.GradeResultEntry> entries) {
        if (entries == null) {
            return;
        }

        for (var entry : entries) {
            var request = entry.getResult();
            var gradeResult = offering.getGradeResults().stream()
                    .filter(candidate -> candidate.getExamPeriod() == entry.getExamPeriod())
                    .findFirst()
                    .orElseGet(() -> {
                        var fresh = new GradeResult();
                        fresh.setLectureOffering(offering);
                        fresh.setExamPeriod(entry.getExamPeriod());
                        offering.getGradeResults().add(fresh);
                        return fresh;
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
            for (var detail : request.getGrades()) {

                if (detail.getMinScore() != null && detail.getMaxScore() != null
                        && detail.getMinScore().compareTo(detail.getMaxScore()) > 0) {
                    throw new BusinessRuleException(LectureOfferingMessages.GRADE_RANGE_INVALID);
                }
                var grade = new GradeDistribution();
                grade.setGradeResult(gradeResult);
                grade.setLetterGrade(detail.getLetterGrade());
                grade.setMinScore(detail.getMinScore());
                grade.setMaxScore(detail.getMaxScore());
                grade.setStudentCount(detail.getStudentCount());
                gradeResult.getGradeDistributions().add(grade);
            }
        }
    }

    private void applyExamStatistics(LectureOffering offering,
                                     List<ImportOfferingsRequestDto.ExamStatisticEntry> entries) {
        if (entries == null) {
            return;
        }

        for (var entry : entries) {
            var request = entry.getStatistic();
            if (request.getAttendedStudentCount() > request.getTotalStudentCount()) {
                throw new BusinessRuleException(LectureOfferingMessages.EXAM_ATTENDED_EXCEEDS_TOTAL);
            }

            var statistic = offering.getExamStatistics().stream()
                    .filter(candidate -> candidate.getExamType() == entry.getExamType())
                    .findFirst()
                    .orElseGet(() -> {
                        var fresh = new ExamStatistic();
                        fresh.setLectureOffering(offering);
                        fresh.setExamType(entry.getExamType());
                        offering.getExamStatistics().add(fresh);
                        return fresh;
                    });

            statistic.setWeightPercent(request.getWeightPercent());
            statistic.setAnnouncedAt(request.getAnnouncedAt());
            statistic.setTotalStudentCount(request.getTotalStudentCount());
            statistic.setAttendedStudentCount(request.getAttendedStudentCount());
            statistic.setFailedByAbsenceCount(
                    request.getFailedByAbsenceCount() == null ? 0 : request.getFailedByAbsenceCount());
            statistic.setAverageScore(request.getAverageScore());
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
