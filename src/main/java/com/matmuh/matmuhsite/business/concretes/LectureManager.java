package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.LectureService;
import com.matmuh.matmuhsite.business.constants.LectureMessages;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.core.utilities.results.SuccessDataResult;
import com.matmuh.matmuhsite.core.utilities.results.SuccessResult;
import com.matmuh.matmuhsite.dataAccess.abstracts.LectureDao;
import com.matmuh.matmuhsite.entities.Lecture;
import com.matmuh.matmuhsite.entities.dtos.RequestLectureDto;
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
    public Result addLecture(RequestLectureDto requestLectureDto) {

        if(requestLectureDto.getName() == null){
            return new SuccessResult(LectureMessages.nameCanotBeNull);
        }


        if(requestLectureDto.getLectureCode() == null){
            return new SuccessResult(LectureMessages.lectureCodeCanotBeNull);
        }

        if(requestLectureDto.getCount() == 0){
            return new SuccessResult(LectureMessages.countCanotBeNull);
        }

        if(requestLectureDto.getCredit() == 0){
            return new SuccessResult(LectureMessages.creditCanotBeNull);
        }

        if(requestLectureDto.getTerm() == 0){
            return new SuccessResult(LectureMessages.termCanotBeNull);
        }

        Lecture lecture = Lecture.builder()
                .name(requestLectureDto.getName())
                .lectureCode(requestLectureDto.getLectureCode())
                .count(requestLectureDto.getCount())
                .credit(requestLectureDto.getCredit())
                .term(requestLectureDto.getTerm())
                .syllabusLink(requestLectureDto.getSyllabusLink())
                .notesLink(requestLectureDto.getNotesLink())
                .build();

        lectureDao.save(lecture);

        return new SuccessResult(LectureMessages.lectureAddSuccess);
    }

    @Override
    public Result updateLecture(RequestLectureDto requestLectureDto) {
        var lecture = getLectureByLectureCode(requestLectureDto.getLectureCode()).getData();

        if(lecture == null){
            return new SuccessResult(LectureMessages.lectureNotFound);
        }

        if(requestLectureDto.getName() == null){
            return new SuccessResult(LectureMessages.nameCanotBeNull);
        }

        if(requestLectureDto.getLectureCode() == null){
            return new SuccessResult(LectureMessages.lectureCodeCanotBeNull);
        }

        if(requestLectureDto.getCount() == 0){
            return new SuccessResult(LectureMessages.countCanotBeNull);
        }

        if(requestLectureDto.getCredit() == 0){
            return new SuccessResult(LectureMessages.creditCanotBeNull);
        }

        if(requestLectureDto.getTerm() == 0){
            return new SuccessResult(LectureMessages.termCanotBeNull);
        }

        lecture.setName(requestLectureDto.getName().isEmpty() ? lecture.getName() : requestLectureDto.getName());
        lecture.setLectureCode(requestLectureDto.getLectureCode().isEmpty() ? lecture.getLectureCode() : requestLectureDto.getLectureCode());
        lecture.setCount(requestLectureDto.getCount() == 0 ? lecture.getCount() : requestLectureDto.getCount());
        lecture.setCredit(requestLectureDto.getCredit() == 0 ? lecture.getCredit() : requestLectureDto.getCredit());
        lecture.setTerm(requestLectureDto.getTerm() == 0 ? lecture.getTerm() : requestLectureDto.getTerm());
        lecture.setSyllabusLink(requestLectureDto.getSyllabusLink().isEmpty() ? lecture.getSyllabusLink() : requestLectureDto.getSyllabusLink());
        lecture.setNotesLink(requestLectureDto.getNotesLink().isEmpty() ? lecture.getNotesLink() : requestLectureDto.getNotesLink());

        lectureDao.save(lecture);

        return new SuccessResult(LectureMessages.lectureUpdateSuccess);
    }

    @Override
    public DataResult<List<Lecture>> getLectures() {
        var result = lectureDao.findAll();

        if (result.isEmpty()){
            return new SuccessDataResult<>(LectureMessages.lectureNotFound);
        }

        return new SuccessDataResult<List<Lecture>>(result, LectureMessages.getMessagesSuccess);
    }

    @Override
    public DataResult<Lecture> getLectureById(int id) {
        var result = lectureDao.findById(id);

        if(result == null){
            return new SuccessDataResult<>(LectureMessages.lectureNotFound);
        }

        return new SuccessDataResult<Lecture>(result, LectureMessages.getLectureByIdSuccess);
    }

    @Override
    public DataResult<List<Lecture>> getLecturesByTerm(int term) {
        var result = lectureDao.findAllByTerm(term);

        if(result.isEmpty()){
            return new SuccessDataResult<>(LectureMessages.lectureNotFound);
        }

        return new SuccessDataResult<>(result, LectureMessages.getMessagesSuccess);
    }

    @Override
    public DataResult<Lecture> getLectureByLectureCode(String lectureCode) {
        var result = lectureDao.findByLectureCode(lectureCode);

        if(result == null){
            return new SuccessDataResult<>(LectureMessages.lectureNotFound);
        }


        return new SuccessDataResult<>(result, LectureMessages.getLectureByLectureCodeSuccess);
    }

    @Override
    public Result deleteLecture(int id) {
        var result = lectureDao.findById(id);

        if(result == null){
            return new SuccessResult(LectureMessages.lectureNotFound);
        }

        this.lectureDao.deleteById(id);
        return new SuccessResult(LectureMessages.lectureDeleteSuccess);
    }
}
