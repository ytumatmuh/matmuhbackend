package com.matmuh.matmuhsite.dataAccess.abstracts;

import com.matmuh.matmuhsite.entities.DegreeLevel;
import com.matmuh.matmuhsite.entities.Lecture;
import com.matmuh.matmuhsite.entities.Semester;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LectureDao extends JpaRepository<Lecture, UUID> {

    Optional<Lecture> findByCode(String code);

    boolean existsByCode(String code);

    Optional<Lecture> findBySlug(String slug);

    boolean existsBySlug(String slug);

    @EntityGraph(attributePaths = {
            "offerings",
            "offerings.staff",
            "offerings.gradeResults",
            "offerings.gradeResults.gradeDistributions",
            "offerings.examStatistics"
    })
    Optional<Lecture> findById(UUID id);

    @EntityGraph(attributePaths = {
            "offerings",
            "offerings.staff",
            "offerings.gradeResults",
            "offerings.gradeResults.gradeDistributions",
            "offerings.examStatistics"
    })
    Optional<Lecture> findWithDetailsByCode(String code);

    @Query("""
            SELECT l FROM Lecture l
            WHERE (:term IS NULL OR l.term = :term)
              AND (:semester IS NULL OR l.semester = :semester)
              AND (:degreeLevel IS NULL OR :degreeLevel MEMBER OF l.degreeLevels)
              AND (CAST(:search AS String) IS NULL
                   OR LOWER(l.name) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%'))
                   OR LOWER(l.code) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%'))
                   OR LOWER(l.about) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')))
            """)
    Page<Lecture> search(@Param("term") Integer term,
                         @Param("semester") Semester semester,
                         @Param("degreeLevel") DegreeLevel degreeLevel,
                         @Param("search") String search,
                         Pageable pageable);
}
