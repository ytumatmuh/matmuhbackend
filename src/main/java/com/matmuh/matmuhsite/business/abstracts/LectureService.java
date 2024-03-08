package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.Lecture;

import java.util.List;

public interface LectureService {

    Result addLecture(Lecture lecture);

    DataResult<List<Lecture>> getLectures();

    DataResult<Lecture> getLectureById(int id);

}
