package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.FileService;
import com.matmuh.matmuhsite.business.abstracts.LectureNoteService;
import com.matmuh.matmuhsite.business.abstracts.SecurityService;
import com.matmuh.matmuhsite.business.constants.LectureNoteMessages;
import com.matmuh.matmuhsite.core.dtos.file.response.FileDto;
import com.matmuh.matmuhsite.core.dtos.lectureNote.request.LectureNoteCreateRequestDto;
import com.matmuh.matmuhsite.core.dtos.lectureNote.response.LectureNoteDto;
import com.matmuh.matmuhsite.core.dtos.lectureNote.response.LectureNoteWithLectureDto;
import com.matmuh.matmuhsite.core.exceptions.ResourceNotFoundException;
import com.matmuh.matmuhsite.core.mappers.LectureNoteMapper;
import com.matmuh.matmuhsite.dataAccess.abstracts.LectureNoteDao;
import com.matmuh.matmuhsite.entities.File;
import com.matmuh.matmuhsite.entities.Lecture;
import com.matmuh.matmuhsite.entities.LectureNote;
import com.matmuh.matmuhsite.entities.User;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LectureNoteManager implements LectureNoteService {

    private final LectureNoteDao lectureNoteDao;

    private final Logger logger = LoggerFactory.getLogger(LectureNoteManager.class);

    private final FileService fileService;

    private final LectureNoteMapper lectureNoteMapper;

    private final SecurityService securityService;


    public LectureNoteManager(LectureNoteDao lectureNoteDao, FileService fileService, LectureNoteMapper lectureNoteMapper, SecurityService securityService) {
        this.lectureNoteDao = lectureNoteDao;
        this.fileService = fileService;
        this.lectureNoteMapper = lectureNoteMapper;
        this.securityService = securityService;
    }


    @Override
    @Transactional
    public LectureNoteDto createLectureNote(Lecture lecture, LectureNoteCreateRequestDto lectureNoteCreateRequestDto, MultipartFile file) {
        logger.info("Creating lecture note for lecture ID: {}", lecture.getId());

        FileDto fileDto = fileService.uploadFile(file);

        LectureNote lectureNote = lectureNoteMapper.toLectureNote(lectureNoteCreateRequestDto, null);

        File fileEntity = fileService.getReference(fileDto.getId());
        lectureNote.setFile(fileEntity);

        lectureNote.setLecture(lecture);

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

        return lectureNoteDao.findByLectureAndIsApproved(lecture, true);
    }

    @Override
    public LectureNoteDto approveLectureNote(UUID lectureNoteId, boolean approved) {
        logger.info("Approving lecture note with ID: {}, approved: {}", lectureNoteId, approved);

        LectureNote lectureNote = lectureNoteDao.findById(lectureNoteId).orElseThrow(() -> {
            logger.error("Lecture note not found with ID: {}", lectureNoteId);
            throw new ResourceNotFoundException(LectureNoteMessages.LECTURE_NOTE_NOT_FOUND);
        });

        lectureNote.setApproved(approved);

        User approver = securityService.getAuthenticatedUserFromContext();

        lectureNote.setApprovedBy(approver);

        LectureNote updatedLectureNote = lectureNoteDao.save(lectureNote);

        logger.info("Lecture note with ID: {} approved successfully", lectureNoteId);

        return lectureNoteMapper.toLectureNoteDto(updatedLectureNote);

    }

    @Override
    public List<LectureNoteWithLectureDto> getAllNotes(Boolean approved) {
        logger.info("Getting all lecture notes with approved filter: {}", approved);

        List<LectureNote> lectureNotes;
        if (approved == null) {
            lectureNotes = lectureNoteDao.findAll();
        } else {
            lectureNotes = lectureNoteDao.findByIsApproved(approved);
        }

        return lectureNotes.stream()
                .map(lectureNoteMapper::toLectureNoteWithLectureDto)
                .collect(Collectors.toList());

    }
}
