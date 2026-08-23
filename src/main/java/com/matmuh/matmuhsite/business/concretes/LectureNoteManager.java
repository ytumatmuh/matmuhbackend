package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.FileService;
import com.matmuh.matmuhsite.business.abstracts.LectureNoteService;
import com.matmuh.matmuhsite.business.abstracts.SecurityService;
import com.matmuh.matmuhsite.business.constants.LectureNoteMessages;
import com.matmuh.matmuhsite.business.constants.LectureOfferingMessages;
import com.matmuh.matmuhsite.core.dtos.common.PageDto;
import com.matmuh.matmuhsite.core.dtos.file.response.FileDto;
import org.springframework.data.domain.Pageable;
import com.matmuh.matmuhsite.core.dtos.lectureNote.request.LectureNoteCreateRequestDto;
import com.matmuh.matmuhsite.core.dtos.lectureNote.response.LectureNoteDto;
import com.matmuh.matmuhsite.core.dtos.lectureNote.response.LectureNoteWithLectureDto;
import com.matmuh.matmuhsite.core.exceptions.BusinessRuleException;
import com.matmuh.matmuhsite.core.exceptions.ResourceNotFoundException;
import com.matmuh.matmuhsite.core.mappers.LectureNoteMapper;
import com.matmuh.matmuhsite.dataAccess.abstracts.LectureNoteDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.LectureOfferingDao;
import com.matmuh.matmuhsite.entities.File;
import com.matmuh.matmuhsite.entities.Lecture;
import com.matmuh.matmuhsite.entities.NoteReviewStatus;
import com.matmuh.matmuhsite.entities.LectureNote;
import com.matmuh.matmuhsite.entities.LectureOffering;
import com.matmuh.matmuhsite.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
public class LectureNoteManager implements LectureNoteService {

    private final LectureNoteDao lectureNoteDao;
    private final Logger logger = LoggerFactory.getLogger(LectureNoteManager.class);
    private final FileService fileService;
    private final LectureNoteMapper lectureNoteMapper;
    private final SecurityService securityService;
    private final LectureOfferingDao lectureOfferingDao;

    public LectureNoteManager(LectureNoteDao lectureNoteDao, FileService fileService, LectureNoteMapper lectureNoteMapper, SecurityService securityService, LectureOfferingDao lectureOfferingDao) {
        this.lectureNoteDao = lectureNoteDao;
        this.fileService = fileService;
        this.lectureNoteMapper = lectureNoteMapper;
        this.securityService = securityService;
        this.lectureOfferingDao = lectureOfferingDao;
    }

    @Override
    @Transactional
    public LectureNoteDto createLectureNote(Lecture lecture, LectureNoteCreateRequestDto lectureNoteCreateRequestDto, MultipartFile file) {
        logger.info("Creating lecture note for lecture ID: {}", lecture.getId());

        LectureOffering offering = null;
        if (lectureNoteCreateRequestDto.getLectureOfferingId() != null) {
            offering = lectureOfferingDao.findById(lectureNoteCreateRequestDto.getLectureOfferingId())
                    .orElseThrow(() -> new ResourceNotFoundException(LectureOfferingMessages.OFFERING_NOT_FOUND));
            if (offering.getLecture() == null || !offering.getLecture().getId().equals(lecture.getId())) {
                throw new BusinessRuleException(LectureOfferingMessages.OFFERING_NOT_BELONGS_TO_LECTURE);
            }
        }

        FileDto fileDto = fileService.uploadFile(file);

        LectureNote lectureNote = lectureNoteMapper.toLectureNote(lectureNoteCreateRequestDto, null);

        File fileEntity = fileService.getReference(fileDto.getId());
        lectureNote.setFile(fileEntity);
        lectureNote.setLecture(lecture);
        lectureNote.setLectureOffering(offering);

        LectureNote savedLectureNote = lectureNoteDao.save(lectureNote);
        logger.info("Lecture note created with ID: {}", savedLectureNote.getId());

        return lectureNoteMapper.toLectureNoteDto(savedLectureNote);
    }

    @Override
    public LectureNote getReference(UUID id) {
        logger.info("Getting reference for lecture note with ID: {}", id);
        return lectureNoteDao.getReferenceById(id);
    }

    @Override
    public List<LectureNote> getLectureNotesByLecture(Lecture lecture) {
        logger.info("Getting lecture notes for lecture ID: {}", lecture.getId());
        var viewer = securityService.getAuthenticatedUserFromContext();
        return lectureNoteDao.findVisibleByLecture(lecture, viewer.getId());
    }

    @Override
    public LectureNoteDto setReviewStatus(UUID lectureNoteId, NoteReviewStatus status) {
        logger.info("Setting review status of lecture note {} to {}", lectureNoteId, status);

        LectureNote lectureNote = lectureNoteDao.findById(lectureNoteId).orElseThrow(() -> {
            logger.error("Lecture note not found with ID: {}", lectureNoteId);
            throw new ResourceNotFoundException(LectureNoteMessages.LECTURE_NOTE_NOT_FOUND);
        });

        lectureNote.setStatus(status);

        if (NoteReviewStatus.PENDING.equals(status)) {
            lectureNote.setApprovedBy(null);
        } else {
            User reviewer = securityService.getAuthenticatedUserFromContext();
            lectureNote.setApprovedBy(reviewer);
        }

        LectureNote updatedLectureNote = lectureNoteDao.save(lectureNote);
        logger.info("Lecture note {} review status set to {}", lectureNoteId, status);

        return lectureNoteMapper.toLectureNoteDto(updatedLectureNote);
    }

    @Override
    @Transactional(readOnly = true)
    public PageDto<LectureNoteWithLectureDto> getAllNotes(NoteReviewStatus status, UUID lectureId, UUID lectureOfferingId,
                                                          UUID staffId, UUID uploaderId, String search, Pageable pageable) {
        logger.info("Getting lecture notes status={} lectureId={} offeringId={} staffId={} uploaderId={} search={} page={}",
                status, lectureId, lectureOfferingId, staffId, uploaderId, search, pageable.getPageNumber());

        var page = lectureNoteDao.search(status, lectureId, lectureOfferingId, staffId, uploaderId,
                search == null || search.isBlank() ? null : search.trim(), pageable);

        return PageDto.of(page, lectureNoteMapper::toLectureNoteWithLectureDto);
    }

    @Override
    @Transactional(readOnly = true)
    public PageDto<LectureNoteWithLectureDto> getMyNotes(NoteReviewStatus status, UUID lectureId, String search, Pageable pageable) {
        var user = securityService.getAuthenticatedUserFromContext();
        logger.info("Getting own lecture notes for user {} lectureId={}", user.getId(), lectureId);

        return getAllNotes(status, lectureId, null, null, user.getId(), search, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public LectureNoteDto getLectureNoteById(UUID lectureNoteId) {
        logger.info("Getting lecture note with ID: {}", lectureNoteId);

        var lectureNote = lectureNoteDao.findById(lectureNoteId).orElseThrow(() -> {
            logger.error("Lecture note not found with ID: {}", lectureNoteId);
            return new ResourceNotFoundException(LectureNoteMessages.LECTURE_NOTE_NOT_FOUND);
        });

        return lectureNoteMapper.toLectureNoteDto(lectureNote);
    }

    @Override
    @Transactional
    public void deleteLectureNote(UUID lectureNoteId) {
        logger.info("Deleting lecture note with ID: {}", lectureNoteId);

        LectureNote lectureNote = lectureNoteDao.findById(lectureNoteId).orElseThrow(() -> {
            logger.error("Lecture note not found with ID: {}", lectureNoteId);
            return new ResourceNotFoundException(LectureNoteMessages.LECTURE_NOTE_NOT_FOUND);
        });

        lectureNoteDao.delete(lectureNote);
        logger.info("Lecture note with ID: {} deleted", lectureNoteId);
    }
}
