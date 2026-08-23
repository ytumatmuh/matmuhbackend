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
import com.matmuh.matmuhsite.dataAccess.abstracts.LectureNoteDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.LectureOfferingDao;
import com.matmuh.matmuhsite.entities.DegreeLevel;
import com.matmuh.matmuhsite.entities.Lecture;
import com.matmuh.matmuhsite.core.dtos.lecture.response.LectureStatisticsDto;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LectureManager implements LectureService {

    private final LectureDao lectureDao;

    private final Logger logger = LoggerFactory.getLogger(LectureManager.class);
    private final LectureMapper lectureMapper;

    private final LectureNoteService lectureNoteService;
    private final LectureNoteMapper lectureNoteMapper;

    private final LectureNoteDao lectureNoteDao;
    private final LectureOfferingDao lectureOfferingDao;


    public LectureManager(LectureDao lectureDao, LectureMapper lectureMapper, LectureNoteService lectureNoteService,
                          LectureNoteMapper lectureNoteMapper, LectureNoteDao lectureNoteDao,
                          LectureOfferingDao lectureOfferingDao) {
        this.lectureDao = lectureDao;
        this.lectureMapper = lectureMapper;
        this.lectureNoteService = lectureNoteService;
        this.lectureNoteMapper = lectureNoteMapper;
        this.lectureNoteDao = lectureNoteDao;
        this.lectureOfferingDao = lectureOfferingDao;
    }


    private void applyBadgeCounts(List<LectureDto> lectures) {
        if (lectures.isEmpty()) {
            return;
        }

        var ids = lectures.stream().map(LectureDto::getId).filter(Objects::nonNull).toList();
        if (ids.isEmpty()) {
            return;
        }

        Map<UUID, Long> noteCounts = lectureNoteDao.countApprovedByLectureIds(ids).stream()
                .collect(Collectors.toMap(row -> (UUID) row[0], row -> ((Number) row[1]).longValue()));


        Map<UUID, Set<String>> terms = new HashMap<>();
        for (var row : lectureOfferingDao.findTermsWithStatistics(ids)) {
            terms.computeIfAbsent((UUID) row[0], key -> new HashSet<>())
                    .add(row[1] + "|" + row[2]);
        }

        for (var dto : lectures) {
            dto.setNoteCount(noteCounts.getOrDefault(dto.getId(), 0L));
            dto.setStatisticsTermCount(terms.getOrDefault(dto.getId(), Set.of()).size());
        }
    }

    @Override
    public LectureDto createLecture(CreateLectureRequestDto createLectureRequestDto) {
        logger.info("Creating lecture with name: {}", createLectureRequestDto.getName());



        boolean exists = lectureDao.existsByCode(createLectureRequestDto.getCode());
        if (exists) {
            logger.error("Lecture creation failed: Lecture with code {} already exists.", createLectureRequestDto.getCode());
            throw new ResourceAlreadyExistsException(LectureMessages.LECTURE_CODE_EXISTS);
        }


        Lecture lecture = lectureMapper.toEntity(createLectureRequestDto);
        if (lecture.getDegreeLevels() == null || lecture.getDegreeLevels().isEmpty()) {
            lecture.setDegreeLevels(new LinkedHashSet<>(DegreeLevel.fromCode(createLectureRequestDto.getCode())));
        }
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

        var dto = lectureMapper.toDto(lecture);
        applyBadgeCounts(List.of(dto));
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public LectureDto getLectureByCode(String code) {
        logger.info("Retrieving lecture with code: {}", code);

        var lecture = lectureDao.findWithDetailsByCode(code).orElseThrow(() -> {
            logger.error("Lecture with code {} not found.", code);
            return new ResourceNotFoundException(LectureMessages.LECTURE_NOT_FOUND);
        });

        var dto = lectureMapper.toDto(lecture);
        applyBadgeCounts(List.of(dto));
        return dto;
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
    public PageDto<LectureDto> getLectures(Integer term, Semester semester, DegreeLevel degreeLevel, String search, Pageable pageable) {
        logger.info("Retrieving lectures term={} semester={} degreeLevel={} search={} page={}", term, semester, degreeLevel, search, pageable.getPageNumber());

        var page = lectureDao.search(term, semester, degreeLevel,
                search == null || search.isBlank() ? null : search.trim(), pageable);

        logger.info("Retrieved {} lectures", page.getTotalElements());

        var result = PageDto.of(page, lectureMapper::toDto);
        applyBadgeCounts(result.getContent());
        return result;
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
