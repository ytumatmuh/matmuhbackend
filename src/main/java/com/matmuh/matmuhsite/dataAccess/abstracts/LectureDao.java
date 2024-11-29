package com.matmuh.matmuhsite.dataAccess.abstracts;

import com.matmuh.matmuhsite.entities.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LectureDao extends JpaRepository<Lecture, UUID> {

    Optional<Lecture> findByLectureCode(String lectureCode);

    List<Lecture> findAllByTerm(int term);

}
