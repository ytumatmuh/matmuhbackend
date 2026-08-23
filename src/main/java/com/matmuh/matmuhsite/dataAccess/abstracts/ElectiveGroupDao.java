package com.matmuh.matmuhsite.dataAccess.abstracts;

import com.matmuh.matmuhsite.entities.DegreeLevel;
import com.matmuh.matmuhsite.entities.ElectiveGroup;
import com.matmuh.matmuhsite.entities.Semester;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ElectiveGroupDao extends JpaRepository<ElectiveGroup, UUID> {

    @EntityGraph(attributePaths = "options")
    Optional<ElectiveGroup> findById(UUID id);

    @EntityGraph(attributePaths = "options")
    Optional<ElectiveGroup> findBySlug(String slug);

    @EntityGraph(attributePaths = "options")
    Optional<ElectiveGroup> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsBySlug(String slug);

    @EntityGraph(attributePaths = "options")
    @Query("""
            SELECT g FROM ElectiveGroup g
            WHERE (:term IS NULL OR g.term = :term)
              AND (:semester IS NULL OR g.semester = :semester)
              AND (:degreeLevel IS NULL OR :degreeLevel MEMBER OF g.degreeLevels)
              AND (CAST(:search AS String) IS NULL
                   OR LOWER(g.name) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%'))
                   OR LOWER(g.code) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')))
            """)
    Page<ElectiveGroup> search(@Param("term") Integer term,
                               @Param("semester") Semester semester,
                               @Param("degreeLevel") DegreeLevel degreeLevel,
                               @Param("search") String search,
                               Pageable pageable);

    @Query("""
            SELECT o.id, COUNT(g)
            FROM ElectiveGroup g JOIN g.options o
            WHERE o.id IN :lectureIds
            GROUP BY o.id
            """)
    List<Object[]> countByLectureIds(@Param("lectureIds") List<UUID> lectureIds);

    @Query("SELECT g FROM ElectiveGroup g JOIN g.options o WHERE o.id = :lectureId")
    List<ElectiveGroup> findByOptionLectureId(@Param("lectureId") UUID lectureId);
}
