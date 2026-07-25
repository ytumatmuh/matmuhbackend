package com.matmuh.matmuhsite.dataAccess.abstracts;

import com.matmuh.matmuhsite.entities.LectureOffering;
import com.matmuh.matmuhsite.entities.Semester;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LectureOfferingDao extends JpaRepository<LectureOffering, UUID>{

    @EntityGraph(attributePaths = {"gradeResults", "gradeResults.gradeDistributions", "examStatistics"})
    Optional<LectureOffering> findWithDetailsById(UUID id);

    List<LectureOffering> findByLectureId(UUID lectureId);

    @EntityGraph(attributePaths = {"lecture", "instructor", "gradeResults", "gradeResults.gradeDistributions", "examStatistics"})
    @Query("""
            SELECT o FROM LectureOffering o
            WHERE o.instructor.id = :instructorId
              AND (CAST(:academicYear AS String) IS NULL OR o.academicYear = CAST(:academicYear AS String))
              AND (:semester IS NULL OR o.semester = :semester)
            ORDER BY o.academicYear DESC, o.semester ASC
            """)
    List<LectureOffering> findByInstructor(@Param("instructorId") UUID instructorId,
                                           @Param("academicYear") String academicYear,
                                           @Param("semester") Semester semester);

}
