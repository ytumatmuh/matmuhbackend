package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.dtos.lecture.request.CreateLectureRequestDto;
import com.matmuh.matmuhsite.core.dtos.lecture.response.LectureDto;
import com.matmuh.matmuhsite.core.dtos.lectureNote.request.LectureNoteCreateRequestDto;
import com.matmuh.matmuhsite.core.dtos.lectureNote.response.LectureNoteDto;
import com.matmuh.matmuhsite.core.dtos.lecture.response.LectureStatisticsDto;
import com.matmuh.matmuhsite.entities.Lecture;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface LectureService {

    LectureDto createLecture(CreateLectureRequestDto createLectureRequestDto);


    LectureNoteDto addNoteToLecture(UUID lectureId, LectureNoteCreateRequestDto lectureNoteCreateRequestDto, MultipartFile file);


    LectureDto getLectureById(UUID lectureId);

    List<LectureNoteDto> getLectureNotes(UUID lectureId);

    List<LectureDto> getLectures();

    LectureStatisticsDto getLectureStatistics(UUID lectureId);

    Lecture getLectureReferenceById(UUID lectureId);
}
