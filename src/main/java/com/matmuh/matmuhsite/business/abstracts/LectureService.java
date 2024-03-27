package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.Lecture;
import com.matmuh.matmuhsite.entities.dtos.RequestLectureDto;

import java.util.List;

public interface LectureService {

    Result addLecture(RequestLectureDto requestLectureDto);

    Result updateLecture(RequestLectureDto requestLectureDto);

    DataResult<List<Lecture>> getLectures();

    DataResult<Lecture> getLectureById(int id);

}
