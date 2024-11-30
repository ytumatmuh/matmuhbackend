package com.matmuh.matmuhsite.webAPI.controllers;

import com.matmuh.matmuhsite.business.abstracts.LectureService;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.Lecture;
import com.matmuh.matmuhsite.webAPI.dtos.RequestLectureDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
                    .lectureCode(lectureDto.getLectureCode())
                    .term(lectureDto.getTerm())
                    .count(lectureDto.getCount())
                    .credit(lectureDto.getCredit())
                    .syllabusLink(lectureDto.getSyllabusLink())
                    .notesLink(lectureDto.getNotesLink())
                    .build();

            var result = lectureService.addLecture(lecture);

            return ResponseEntity.status(result.getHttpStatus()).body(result);
    }


}
