package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.dtos.lectureNote.request.LectureNoteCreateRequestDto;
import com.matmuh.matmuhsite.core.dtos.lectureNote.response.LectureNoteDto;
import com.matmuh.matmuhsite.entities.Lecture;
import com.matmuh.matmuhsite.entities.LectureNote;
import com.matmuh.matmuhsite.core.dtos.lectureNote.response.LectureNoteWithLectureDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface LectureNoteService {
    LectureNoteDto createLectureNote(Lecture lecture, LectureNoteCreateRequestDto lectureNoteCreateRequestDto, MultipartFile file);

    LectureNote getReference(UUID id);

    List<LectureNote> getLectureNotesByLecture(Lecture lecture);

    LectureNoteDto approveLectureNote(UUID lectureNoteId, boolean approved);

    List<LectureNoteWithLectureDto> getAllNotes(Boolean approved);

}
