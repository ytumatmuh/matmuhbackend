package com.matmuh.matmuhsite.webAPI.controllers;

import com.matmuh.matmuhsite.business.abstracts.LectureService;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.Lecture;
import com.matmuh.matmuhsite.webAPI.dtos.RequestLectureDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/lectures")
public class LectureController {

    private final LectureService lectureService;

    public LectureController(LectureService lectureService) {
        this.lectureService = lectureService;
    }

    @GetMapping("/getLectures")
    public ResponseEntity<DataResult<List<Lecture>>> getLectures(){
        var result = lectureService.getLectures();

        return ResponseEntity.status(result.getHttpStatus()).body(result);

    }

    @PostMapping("/addLecture")
    public ResponseEntity<Result> addLecture(@RequestBody RequestLectureDto lectureDto){

            Lecture lecture = Lecture.builder()
                    .id(lectureDto.getId())
                    .name(lectureDto.getName())
                    .code(lectureDto.getCode())
                    .term(lectureDto.getTerm())
                    .count(lectureDto.getCount())
                    .credit(lectureDto.getCredit())
                    .syllabusLink(lectureDto.getSyllabusLink())
                    .notesLink(lectureDto.getNotesLink())
                    .build();

            var result = lectureService.addLecture(lecture);

            return ResponseEntity.status(result.getHttpStatus()).body(result);
    }


    @PutMapping("/updateLectureById/{id}")
    public ResponseEntity<Result> updateLectureById(@PathVariable UUID id, @RequestBody RequestLectureDto requestLectureDto) {
        Lecture lecture = Lecture.builder()
                .id(id)
                .name(requestLectureDto.getName())
                .code(requestLectureDto.getCode())
                .term(requestLectureDto.getTerm())
                .count(requestLectureDto.getCount())
                .credit(requestLectureDto.getCredit())
                .syllabusLink(requestLectureDto.getSyllabusLink())
                .notesLink(requestLectureDto.getNotesLink())
                .build();

        var result = lectureService.updateLecture(lecture);

        return ResponseEntity.status(result.getHttpStatus()).body(result);

    }

    @DeleteMapping("/deleteLectureById/{id}")
    public ResponseEntity<Result> deleteLectureById(@PathVariable UUID id){

        var result = lectureService.deleteLectureById(id);

        return ResponseEntity.status(result.getHttpStatus()).body(result);

    }

}
