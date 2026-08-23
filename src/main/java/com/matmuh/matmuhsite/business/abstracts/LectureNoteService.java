package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.dtos.common.PageDto;
import com.matmuh.matmuhsite.core.dtos.lectureNote.request.LectureNoteCreateRequestDto;
import org.springframework.data.domain.Pageable;
import com.matmuh.matmuhsite.core.dtos.lectureNote.response.LectureNoteDto;
import com.matmuh.matmuhsite.entities.Lecture;
import com.matmuh.matmuhsite.entities.NoteReviewStatus;
import com.matmuh.matmuhsite.entities.LectureNote;
import com.matmuh.matmuhsite.core.dtos.lectureNote.response.LectureNoteWithLectureDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface LectureNoteService {
    LectureNoteDto createLectureNote(Lecture lecture, LectureNoteCreateRequestDto lectureNoteCreateRequestDto, MultipartFile file);

    LectureNote getReference(UUID id);

    List<LectureNote> getLectureNotesByLecture(Lecture lecture);

    LectureNoteDto setReviewStatus(UUID lectureNoteId, NoteReviewStatus status);

    PageDto<LectureNoteWithLectureDto> getAllNotes(NoteReviewStatus status, UUID lectureId, UUID lectureOfferingId,
                                                  UUID staffId, UUID uploaderId, String search, Pageable pageable);


    PageDto<LectureNoteWithLectureDto> getMyNotes(NoteReviewStatus status, UUID lectureId, String search, Pageable pageable);

    LectureNoteDto getLectureNoteById(UUID lectureNoteId);

    void deleteLectureNote(UUID lectureNoteId);

}
