package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.LectureService;
import com.matmuh.matmuhsite.business.constants.LectureMessages;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.core.utilities.results.SuccessDataResult;
import com.matmuh.matmuhsite.core.utilities.results.SuccessResult;
import com.matmuh.matmuhsite.dataAccess.abstracts.LectureDao;
import com.matmuh.matmuhsite.entities.Lecture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LectureManager implements LectureService {
    private final LectureDao lectureDao;

    @Autowired
    public LectureManager(LectureDao lectureDao) {
        this.lectureDao = lectureDao;
    }

    @Override
    public Result addLecture(Lecture lecture) {
        lectureDao.save(lecture);

        return new SuccessResult(LectureMessages.lectureAddSuccess);
    }

    @Override
    public DataResult<List<Lecture>> getLectures() {
        var result = lectureDao.findAll();

        return new SuccessDataResult<List<Lecture>>(result, LectureMessages.getMessagesSuccess);
    }

    @Override
    public DataResult<Lecture> getLectureById(int id) {
        var result = lectureDao.findById(id);

        return new SuccessDataResult<Lecture>(result, LectureMessages.getLectureByIdSuccess);
    }
}
