package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.LectureNoteService;
import com.matmuh.matmuhsite.business.abstracts.LectureOfferingService;
import com.matmuh.matmuhsite.business.abstracts.LectureService;
import com.matmuh.matmuhsite.business.constants.LectureMessages;
import com.matmuh.matmuhsite.core.dtos.common.PageDto;
import com.matmuh.matmuhsite.core.dtos.lecture.request.CreateLectureRequestDto;
import com.matmuh.matmuhsite.entities.Semester;
import org.springframework.data.domain.Pageable;
import com.matmuh.matmuhsite.core.dtos.lecture.response.LectureDto;
import com.matmuh.matmuhsite.core.dtos.lectureNote.request.LectureNoteCreateRequestDto;
import com.matmuh.matmuhsite.core.dtos.lectureNote.response.LectureNoteDto;
import com.matmuh.matmuhsite.core.exceptions.ResourceAlreadyExistsException;
import com.matmuh.matmuhsite.core.helpers.UniqueSlugResolver;
import com.matmuh.matmuhsite.core.exceptions.ResourceNotFoundException;
import com.matmuh.matmuhsite.core.mappers.LectureMapper;
import com.matmuh.matmuhsite.core.mappers.LectureNoteMapper;
import com.matmuh.matmuhsite.dataAccess.abstracts.LectureDao;
import com.matmuh.matmuhsite.entities.Lecture;
import com.matmuh.matmuhsite.core.dtos.lecture.response.LectureStatisticsDto;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class LectureManager implements LectureService {

    private final LectureDao lectureDao;

    private final Logger logger = LoggerFactory.getLogger(LectureManager.class);
    private final LectureMapper lectureMapper;

    private final LectureNoteService lectureNoteService;
    private final LectureNoteMapper lectureNoteMapper;


    public LectureManager(LectureDao lectureDao, LectureMapper lectureMapper, LectureNoteService lectureNoteService, LectureNoteMapper lectureNoteMapper) {
        this.lectureDao = lectureDao;
        this.lectureMapper = lectureMapper;
        this.lectureNoteService = lectureNoteService;
        this.lectureNoteMapper = lectureNoteMapper;
    }

    @Override
    public LectureDto createLecture(CreateLectureRequestDto createLectureRequestDto) {
        logger.info("Creating lecture with name: {}", createLectureRequestDto.getName());


        //check if lecture with the code exists
        boolean exists = lectureDao.existsByCode(createLectureRequestDto.getCode());
        if (exists) {
            logger.error("Lecture creation failed: Lecture with code {} already exists.", createLectureRequestDto.getCode());
            throw new ResourceAlreadyExistsException(LectureMessages.LECTURE_CODE_EXISTS);
        }


        Lecture lecture = lectureMapper.toEntity(createLectureRequestDto);
        lecture.setSlug(UniqueSlugResolver.resolve(
                createLectureRequestDto.getSlug(),
                createLectureRequestDto.getCode(),
                createLectureRequestDto.getName(),
                lectureDao::existsBySlug,
                LectureMessages.SLUG_INVALID,
                LectureMessages.SLUG_EXISTS));

        Lecture savedLecture = lectureDao.save(lecture);
        logger.info("Lecture created successfully with ID: {}", savedLecture.getId());

        return lectureMapper.toDto(savedLecture);
    }

    @Override
    @Transactional
    public LectureNoteDto addNoteToLecture(UUID lectureId, LectureNoteCreateRequestDto lectureNoteCreateRequestDto, MultipartFile file) {
        logger.info("Adding note to lecture with ID: {}", lectureId);

        var lecture = lectureDao.findById(lectureId).orElseThrow(() -> {
            logger.error("Lecture with ID {} not found.", lectureId);
            return new ResourceNotFoundException(LectureMessages.LECTURE_NOT_FOUND);
        });

        var createdNote = lectureNoteService.createLectureNote(lecture, lectureNoteCreateRequestDto, file);
        logger.info("Note added to lecture with ID: {}", lectureId);

        return createdNote;
    }

    @Override
    public LectureDto getLectureById(UUID lectureId) {
        logger.info("Retrieving lecture with ID: {}", lectureId);

        var lecture = lectureDao.findById(lectureId).orElseThrow(() -> {
            logger.error("Lecture with ID {} not found.", lectureId);
            return new ResourceNotFoundException(LectureMessages.LECTURE_NOT_FOUND);
        });

        return lectureMapper.toDto(lecture);
    }

    @Override
    public List<LectureNoteDto> getLectureNotes(UUID lectureId) {
        logger.info("Retrieving notes for lecture with ID: {}", lectureId);


        var lecture = lectureDao.findById(lectureId).orElseThrow(() -> {
            logger.error("Lecture with ID {} not found.", lectureId);
            return new ResourceNotFoundException(LectureMessages.LECTURE_NOT_FOUND);
        });

        var notes = lectureNoteService.getLectureNotesByLecture(lecture);

        logger.info("Retrieved {} notes for lecture with ID: {}", notes.size(), lectureId);

        return lectureNoteMapper.toLectureNoteDtos(notes);

    }

    @Override
    @Transactional
    public LectureDto updateLecture(UUID lectureId, com.matmuh.matmuhsite.core.dtos.lecture.request.UpdateLectureRequestDto request) {
        logger.info("Updating lecture with ID: {}", lectureId);

        var lecture = lectureDao.findById(lectureId).orElseThrow(() -> {
            logger.error("Lecture with ID {} not found.", lectureId);
            return new ResourceNotFoundException(LectureMessages.LECTURE_NOT_FOUND);
        });

        if (request.getCode() != null && !request.getCode().equals(lecture.getCode())
                && lectureDao.existsByCode(request.getCode())) {
            logger.error("Lecture update failed: code {} already exists.", request.getCode());
            throw new ResourceAlreadyExistsException(LectureMessages.LECTURE_CODE_EXISTS);
        }

        lectureMapper.updateLectureFromDto(request, lecture);
        var saved = lectureDao.save(lecture);

        logger.info("Lecture updated with ID: {}", saved.getId());
        return lectureMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteLecture(UUID lectureId) {
        logger.info("Deleting lecture with ID: {}", lectureId);

        var lecture = lectureDao.findById(lectureId).orElseThrow(() -> {
            logger.error("Lecture with ID {} not found.", lectureId);
            return new ResourceNotFoundException(LectureMessages.LECTURE_NOT_FOUND);
        });

        lectureDao.delete(lecture);
        logger.info("Lecture soft deleted with ID: {}", lectureId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageDto<LectureDto> getLectures(Integer term, Semester semester, String search, Pageable pageable) {
        logger.info("Retrieving lectures term={} semester={} search={} page={}", term, semester, search, pageable.getPageNumber());

        var page = lectureDao.search(term, semester,
                search == null || search.isBlank() ? null : search.trim(), pageable);

        logger.info("Retrieved {} lectures", page.getTotalElements());

        return PageDto.of(page, lectureMapper::toDto);
    }

    @Override
    public LectureStatisticsDto getLectureStatistics(UUID lectureId) {
        logger.info("Retrieving statistics for lecture with ID: {}", lectureId);

        Lecture lecture = lectureDao.findById(lectureId).orElseThrow(() -> {
            logger.error("Lecture with ID {} not found.", lectureId);
            return new ResourceNotFoundException(LectureMessages.LECTURE_NOT_FOUND);
        });

        logger.info("Retrieved statistics for lecture with ID: {}", lectureId);

        return lectureMapper.toLectureStatisticsDto(lecture);

    }

    @Override
    public Lecture getLectureReferenceById(UUID lectureId) {
        logger.info("Retrieving reference for lecture with ID: {}", lectureId);

        return lectureDao.getReferenceById(lectureId);
    }
}
