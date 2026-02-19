package com.matmuh.matmuhsite.webAPI.controllers;

import com.matmuh.matmuhsite.business.abstracts.LectureService;
import com.matmuh.matmuhsite.business.constants.LectureNoteMessages;
import com.matmuh.matmuhsite.core.dtos.lecture.request.CreateLectureRequestDto;
import com.matmuh.matmuhsite.core.dtos.lecture.response.LectureDto;
import com.matmuh.matmuhsite.core.dtos.lectureNote.request.LectureNoteCreateRequestDto;
import com.matmuh.matmuhsite.core.dtos.lectureNote.response.LectureNoteDto;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.SuccessDataResult;
import com.matmuh.matmuhsite.core.dtos.lecture.response.LectureStatisticsDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/lectures")
public class LectureController {

    private final LectureService lectureService;


    public LectureController(LectureService lectureService) {
        this.lectureService = lectureService;
    }


    @PostMapping()
    public ResponseEntity<DataResult<LectureDto>> createLecture(@RequestBody CreateLectureRequestDto createLectureRequestDto) {

        var createdLecture = lectureService.createLecture(createLectureRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessDataResult<>(createdLecture, LectureNoteMessages.LESSON_CREATE_SUCCESS, HttpStatus.CREATED));

    }

    @GetMapping()
    public ResponseEntity<DataResult<List<LectureDto>>> getLectures(){
        var lectures = lectureService.getLectures();

        return ResponseEntity.status(HttpStatus.OK).body(new SuccessDataResult<>(lectures, LectureNoteMessages.LECTURES_FETCH_SUCCESS, HttpStatus.OK));
    }

    @PostMapping(value = "/{lectureId}/notes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DataResult<LectureNoteDto>> addNoteToLecture(@PathVariable UUID lectureId, @RequestPart("data") LectureNoteCreateRequestDto lectureNoteCreateRequestDto, @RequestPart("file") MultipartFile file){

        var createdLectureNote = lectureService.addNoteToLecture(lectureId, lectureNoteCreateRequestDto, file);

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessDataResult<>(createdLectureNote, LectureNoteMessages.LECTURE_NOTE_CREATE_SUCCESS, HttpStatus.CREATED));

    }

    @GetMapping("/{lectureId}/notes")
    public ResponseEntity<DataResult<List<LectureNoteDto>>> getLectureNotes(@PathVariable UUID lectureId) {

        var lectureNotes = lectureService.getLectureNotes(lectureId);

        return ResponseEntity.status(HttpStatus.OK).body(new SuccessDataResult<>(lectureNotes, LectureNoteMessages.LECTURE_NOTES_FETCH_SUCCESS, HttpStatus.OK));

    }

    @GetMapping("/{lectureId}/statistics")
    public ResponseEntity<DataResult<LectureStatisticsDto>> getLectureStatistics(@PathVariable UUID lectureId) {

        var lectureStatistics = lectureService.getLectureStatistics(lectureId);

        return ResponseEntity.status(HttpStatus.OK).body(new SuccessDataResult<>(lectureStatistics, LectureNoteMessages.LECTURE_STATISTICS_FETCH_SUCCESS, HttpStatus.OK));

    }
}
