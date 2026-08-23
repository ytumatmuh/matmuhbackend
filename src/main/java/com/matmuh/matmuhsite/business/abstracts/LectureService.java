package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.dtos.common.PageDto;
import com.matmuh.matmuhsite.core.dtos.lecture.request.CreateLectureRequestDto;
import com.matmuh.matmuhsite.entities.DegreeLevel;
import com.matmuh.matmuhsite.entities.LectureCategory;
import com.matmuh.matmuhsite.entities.LectureType;
import com.matmuh.matmuhsite.entities.Semester;
import org.springframework.data.domain.Pageable;
import com.matmuh.matmuhsite.core.dtos.lecture.request.UpdateLectureRequestDto;
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

    LectureDto getLectureByCode(String code);

    List<LectureNoteDto> getLectureNotes(UUID lectureId);

    LectureDto updateLecture(UUID lectureId, UpdateLectureRequestDto updateLectureRequestDto);

    void deleteLecture(UUID lectureId);


    void applyBadgeCounts(List<LectureDto> lectures);

    PageDto<LectureDto> getLectures(Integer term, Semester semester, DegreeLevel degreeLevel,
                                    LectureType type, LectureCategory category, String search, Pageable pageable);

    LectureStatisticsDto getLectureStatistics(UUID lectureId);

    Lecture getLectureReferenceById(UUID lectureId);
}
