package com.matmuh.matmuhsite.dataAccess.abstracts;

import com.matmuh.matmuhsite.entities.Lecture;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LectureDao extends JpaRepository<Lecture, UUID> {

    Optional<Lecture> findByCode(String code);

    List<Lecture> findAllByTerm(int term);

    boolean existsByCode(String code);


    @EntityGraph(attributePaths = {
            "offerings",
            "offerings.instructor",
            "offerings.gradeDistributions"
    })
    Optional<Lecture> findById(UUID id);

}
