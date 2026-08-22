package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.dtos.examStatistic.request.SaveExamStatisticRequestDto;
import com.matmuh.matmuhsite.core.dtos.gradeDistribution.request.SaveGradeResultRequestDto;
import com.matmuh.matmuhsite.core.dtos.lectureOfferings.request.CreateLectureOfferingRequestDto;
import com.matmuh.matmuhsite.core.dtos.lectureOfferings.request.UpdateLectureOfferingRequestDto;
import com.matmuh.matmuhsite.core.dtos.lectureOfferings.response.LectureOfferingDto;
import com.matmuh.matmuhsite.entities.ExamPeriod;
import com.matmuh.matmuhsite.entities.ExamType;
import com.matmuh.matmuhsite.entities.LectureOffering;
import com.matmuh.matmuhsite.entities.Semester;

import java.util.List;
import java.util.UUID;

public interface LectureOfferingService {

    LectureOfferingDto createOffering(UUID lectureId, CreateLectureOfferingRequestDto createOfferingRequestDto);

    LectureOfferingDto updateOffering(UUID offeringId, UpdateLectureOfferingRequestDto updateOfferingRequestDto);

    void deleteOffering(UUID offeringId);

    LectureOfferingDto saveGradeResult(UUID offeringId, ExamPeriod examPeriod, SaveGradeResultRequestDto saveGradeResultRequestDto);

    void deleteGradeResult(UUID offeringId, ExamPeriod examPeriod);

    LectureOfferingDto saveExamStatistic(UUID offeringId, ExamType examType, SaveExamStatisticRequestDto saveExamStatisticRequestDto);

    void deleteExamStatistic(UUID offeringId, ExamType examType);

    LectureOfferingDto getOfferingById(UUID offeringId);

    List<LectureOfferingDto> getOfferingsByLecture(UUID lectureId);

    List<LectureOfferingDto> getOfferingsByStaff(UUID staffId, String academicYear, Semester semester);

    LectureOffering getOfferingReferenceById(UUID offeringId);

}
