package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.InstructorService;
import com.matmuh.matmuhsite.business.abstracts.LectureOfferingService;
import com.matmuh.matmuhsite.business.abstracts.LectureService;
import com.matmuh.matmuhsite.business.constants.LectureOfferingMessages;
import com.matmuh.matmuhsite.core.dtos.gradeDistribution.request.GradeSubmissionRequestDto;
import com.matmuh.matmuhsite.core.dtos.instructor.response.InstructorDto;
import com.matmuh.matmuhsite.core.dtos.lecture.response.LectureDto;
import com.matmuh.matmuhsite.core.dtos.lectureOfferings.request.CreateLectureOfferingRequestDto;
import com.matmuh.matmuhsite.core.dtos.lectureOfferings.response.LectureOfferingDto;
import com.matmuh.matmuhsite.core.exceptions.ResourceNotFoundException;
import com.matmuh.matmuhsite.core.mappers.LectureOfferingMapper;
import com.matmuh.matmuhsite.dataAccess.abstracts.LectureOfferingDao;
import com.matmuh.matmuhsite.entities.GradeDistribution;
import com.matmuh.matmuhsite.entities.LectureOffering;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;
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
    public LectureOfferingDto createOffering(CreateLectureOfferingRequestDto createOfferingRequestDto) {
        logger.info("Creating lecture offering for lecture ID: {}", createOfferingRequestDto.getLectureId());

        LectureDto lecture = lectureService.getLectureById(createOfferingRequestDto.getLectureId());

        logger.info("Lecture found: {} - {}", lecture.getCode(), lecture.getName());

        InstructorDto instructor = instructorService.getInstructorById(createOfferingRequestDto.getInstructorId());

        logger.info("Instructor found: {} {}", instructor.getFirstName(), instructor.getLastName());

        LectureOffering lectureOffering = new LectureOffering();
        lectureOffering.setLecture(lectureService.getLectureReferenceById(lecture.getId()));
        lectureOffering.setInstructor(instructorService.getInstructorReferenceById(instructor.getId()));
        lectureOffering.setAcademicYear(createOfferingRequestDto.getAcademicYear());
        lectureOffering.setGroupNumber(createOfferingRequestDto.getGroupNumber());

        LectureOffering savedOffering = lectureOfferingDao.save(lectureOffering);

        logger.info("Lecture offering created with ID: {}", savedOffering.getId());

        return lectureOfferingMapper.toLectureOfferingDto(savedOffering);
    }

    @Override
    public LectureOfferingDto addGradesToOffering(UUID offeringId, GradeSubmissionRequestDto gradeSubmissionRequestDto) {
        logger.info("Adding grades to lecture offering with ID: {}", offeringId);

        LectureOffering lectureOffering = lectureOfferingDao.findById(offeringId).orElseThrow(()->{
            logger.error("Lecture offering not found with ID: {}", offeringId);
            return new ResourceNotFoundException(LectureOfferingMessages.OFFERING_NOT_FOUND);
        });

        logger.info("Lecture offering found: Academic year: {}, Group: {}", lectureOffering.getAcademicYear(), lectureOffering.getGroupNumber());


        Set<GradeDistribution> gradeEntities = gradeSubmissionRequestDto.getGrades().stream().map(
                gradeDto -> {
                    GradeDistribution grade = new GradeDistribution();
                    grade.setLectureOffering(lectureOffering);
                    grade.setExamPeriod(gradeSubmissionRequestDto.getExamPeriod());
                    grade.setLetterGrade(gradeDto.getLetterGrade());
                    grade.setMinScore(gradeDto.getMinScore());
                    grade.setMaxScore(gradeDto.getMaxScore());
                    grade.setStudentCount(gradeDto.getStudentCount());
                    return grade;
                }
        ).collect(Collectors.toSet());

        lectureOffering.getGradeDistributions().addAll(gradeEntities);

        LectureOffering savedOffering = lectureOfferingDao.save(lectureOffering);

        logger.info("Grades added to lecture offering with ID: {}", savedOffering.getId());

        return lectureOfferingMapper.toLectureOfferingDto(savedOffering);


    }
}
