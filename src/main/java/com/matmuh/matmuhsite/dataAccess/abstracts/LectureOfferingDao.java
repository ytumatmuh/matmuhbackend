package com.matmuh.matmuhsite.dataAccess.abstracts;

import com.matmuh.matmuhsite.entities.LectureOffering;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LectureOfferingDao extends JpaRepository<LectureOffering, UUID>{

    @EntityGraph(attributePaths = {"gradeDistributions"})
    Optional<LectureOffering> findWithGradeDistributionsById(UUID id);

}
