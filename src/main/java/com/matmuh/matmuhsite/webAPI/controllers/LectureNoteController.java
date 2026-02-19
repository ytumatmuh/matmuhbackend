package com.matmuh.matmuhsite.webAPI.controllers;

import com.matmuh.matmuhsite.business.abstracts.LectureNoteService;
import com.matmuh.matmuhsite.business.constants.LectureNoteMessages;
import com.matmuh.matmuhsite.core.dtos.lectureNote.request.LectureNoteApproveRequestDto;
import com.matmuh.matmuhsite.core.dtos.lectureNote.response.LectureNoteDto;
import com.matmuh.matmuhsite.core.dtos.lectureNote.response.LectureNoteWithLectureDto;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.SuccessDataResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lecture-notes")
public class LectureNoteController {

    private final LectureNoteService lectureNoteService;


    public LectureNoteController(LectureNoteService lectureNoteService) {
        this.lectureNoteService = lectureNoteService;
    }


    @PutMapping("/{lectureNoteId}/approve")
    public ResponseEntity<DataResult<LectureNoteDto>> approveLectureNote(@PathVariable UUID lectureNoteId,@RequestBody LectureNoteApproveRequestDto approved) {

        var approvedLectureNote = lectureNoteService.approveLectureNote(lectureNoteId, approved.isApproved());

        return ResponseEntity.ok(new SuccessDataResult<>(approvedLectureNote, LectureNoteMessages.LECTURE_NOTE_APPROVE_SUCCESS, HttpStatus.OK));

    }

    @GetMapping()
    public ResponseEntity<DataResult<List<LectureNoteWithLectureDto>>> getLectureNotes(@RequestParam(required = false) UUID lectureId, @RequestParam(required = false) Boolean approved){

        var result = lectureNoteService.getAllNotes(approved);

        return ResponseEntity.ok(new SuccessDataResult<>(result, LectureNoteMessages.LECTURE_NOTES_FETCH_SUCCESS, HttpStatus.OK));

    }



}
