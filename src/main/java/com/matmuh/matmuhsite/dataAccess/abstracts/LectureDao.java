package com.matmuh.matmuhsite.dataAccess.abstracts;

import com.matmuh.matmuhsite.entities.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LectureDao extends JpaRepository<Lecture, Integer> {

    Lecture findById(int id);

    Lecture findByLectureCode(String lectureCode);

    List<Lecture> findAllByTerm(int term);

}
