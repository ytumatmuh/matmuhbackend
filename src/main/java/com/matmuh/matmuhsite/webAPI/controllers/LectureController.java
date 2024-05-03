package com.matmuh.matmuhsite.webAPI.controllers;

import com.matmuh.matmuhsite.business.abstracts.LectureService;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.Lecture;
import com.matmuh.matmuhsite.entities.dtos.RequestLectureDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/lectures")
public class LectureController {

    @Autowired
    private LectureService lectureService;

    @GetMapping("/getLectures")
    public DataResult<List<Lecture>> getLectures(){
        return lectureService.getLectures();
    }

    @PostMapping("/addLecture")
    public Result addLecture(@RequestBody RequestLectureDto lectureDto){
        return lectureService.addLecture(lectureDto);
    }


}
