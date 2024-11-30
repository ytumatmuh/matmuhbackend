package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.Lecture;

import java.util.List;
import java.util.UUID;

public interface LectureService {

    Result addLecture(Lecture lecture);

    Result updateLecture(Lecture Lecture);

    DataResult<List<Lecture>> getLectures();

    DataResult<Lecture> getLectureById(UUID id);

    DataResult<List<Lecture>> getLecturesByTerm(int term);

    DataResult<Lecture> getLectureByLectureCode(String lectureCode);

    Result deleteLecture(UUID id);



}
