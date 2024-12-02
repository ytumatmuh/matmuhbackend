package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.LectureService;
import com.matmuh.matmuhsite.business.constants.LectureMessages;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.core.utilities.results.SuccessDataResult;
import com.matmuh.matmuhsite.core.utilities.results.SuccessResult;
import com.matmuh.matmuhsite.dataAccess.abstracts.LectureDao;
import com.matmuh.matmuhsite.entities.Lecture;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LectureManager implements LectureService {

    private final LectureDao lectureDao;

    public LectureManager(LectureDao lectureDao) {
        this.lectureDao = lectureDao;
    }

    @Override
    public Result addLecture(Lecture lecture) {

        if(lecture.getName() == null){
            return new SuccessResult(LectureMessages.nameCanotBeNull, HttpStatus.BAD_REQUEST);
        }

        if(lecture.getCode() == null){
            return new SuccessResult(LectureMessages.lectureCodeCanotBeNull, HttpStatus.BAD_REQUEST);
        }

        if(lecture.getCount() == 0){
            return new SuccessResult(LectureMessages.countCanotBeNull, HttpStatus.BAD_REQUEST);
        }

        if(lecture.getCredit() == 0){
            return new SuccessResult(LectureMessages.creditCanotBeNull, HttpStatus.BAD_REQUEST);
        }

        if(lecture.getTerm() == 0){
            return new SuccessResult(LectureMessages.termCanotBeNull, HttpStatus.BAD_REQUEST);
        }

        lectureDao.save(lecture);

        return new SuccessResult(LectureMessages.lectureAddSuccess, HttpStatus.CREATED);
    }

    @Override
    public Result updateLecture(Lecture lecture) {
       var result = lectureDao.findById(lecture.getId());
       if (result.isEmpty()){
           return new SuccessResult(LectureMessages.lectureNotFound, HttpStatus.NOT_FOUND);
         }

        lectureDao.save(lecture);

        return new SuccessResult(LectureMessages.lectureUpdateSuccess, HttpStatus.OK);
    }

    @Override
    public DataResult<List<Lecture>> getLectures() {
        var result = lectureDao.findAll();

        if (result.isEmpty()){
            return new SuccessDataResult<>(LectureMessages.lectureNotFound, HttpStatus.NOT_FOUND);
        }

        return new SuccessDataResult<List<Lecture>>(result, LectureMessages.getMessagesSuccess, HttpStatus.OK);
    }

    @Override
    public DataResult<Lecture> getLectureById(UUID id) {
        var result = lectureDao.findById(id);

        if(result == null){
            return new SuccessDataResult<>(LectureMessages.lectureNotFound, HttpStatus.NOT_FOUND);
        }

        return new SuccessDataResult<Lecture>(result.get(), LectureMessages.getLectureByIdSuccess, HttpStatus.OK);
    }

    @Override
    public DataResult<List<Lecture>> getLecturesByTerm(int term) {
        var result = lectureDao.findAllByTerm(term);

        if(result.isEmpty()){
            return new SuccessDataResult<>(LectureMessages.lectureNotFound, HttpStatus.NOT_FOUND);
        }

        return new SuccessDataResult<>(result, LectureMessages.getMessagesSuccess, HttpStatus.OK);
    }

    @Override
    public DataResult<Lecture> getLecturesByCode(String lectureCode) {
        var result = lectureDao.findByCode(lectureCode);

        if(result.isEmpty()){
            return new SuccessDataResult<>(LectureMessages.lectureNotFound, HttpStatus.NOT_FOUND);
        }


        return new SuccessDataResult<>(result.get(), LectureMessages.getLectureByLectureCodeSuccess, HttpStatus.OK);
    }

    @Override
    public Result deleteLectureById(UUID id) {
        var result = lectureDao.findById(id);

        if(result.isEmpty()){
            return new SuccessResult(LectureMessages.lectureNotFound, HttpStatus.NOT_FOUND);
        }

        this.lectureDao.deleteById(id);
        return new SuccessResult(LectureMessages.lectureDeleteSuccess, HttpStatus.OK);
    }
}
