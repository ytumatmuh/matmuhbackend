package com.matmuh.matmuhsite.dataAccess.abstracts;

import com.matmuh.matmuhsite.entities.Enrollment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnrollmentDao extends JpaRepository<Enrollment, UUID> {

    @EntityGraph(attributePaths = {"lectureOffering", "lectureOffering.lecture", "lectureOffering.staff"})
    List<Enrollment> findByUserId(UUID userId);

    Optional<Enrollment> findByUserIdAndLectureOfferingId(UUID userId, UUID lectureOfferingId);

    boolean existsByUserIdAndLectureOfferingId(UUID userId, UUID lectureOfferingId);
}
