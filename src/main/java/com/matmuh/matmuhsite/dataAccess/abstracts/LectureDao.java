package com.matmuh.matmuhsite.dataAccess.abstracts;

import com.matmuh.matmuhsite.entities.Program;
import com.matmuh.matmuhsite.entities.DegreeLevel;
import com.matmuh.matmuhsite.entities.InstructionLanguage;
import com.matmuh.matmuhsite.entities.Lecture;
import com.matmuh.matmuhsite.entities.LectureCategory;
import com.matmuh.matmuhsite.entities.LectureType;
import com.matmuh.matmuhsite.entities.Semester;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LectureDao extends JpaRepository<Lecture, UUID> {

    Optional<Lecture> findByCode(String code);

    Optional<Lecture> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

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
    Optional<Lecture> findWithDetailsByCodeIgnoreCase(String code);

    @Query("""
            SELECT l FROM Lecture l
            WHERE (:term IS NULL OR l.term = :term)
              AND (:semester IS NULL OR l.semester = :semester)
              AND (:degreeLevel IS NULL OR :degreeLevel MEMBER OF l.degreeLevels)
              AND (:program IS NULL OR :program MEMBER OF l.programs)
              AND (:type IS NULL OR l.type = :type)
              AND (:category IS NULL OR l.category = :category)
              AND (:filterByLanguage = false OR EXISTS (
                       SELECT taught.id FROM Lecture taught JOIN taught.languages lang
                       WHERE taught.id = l.id AND lang IN :languages))
              AND (CAST(:search AS String) IS NULL
                   OR LOWER(l.name) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%'))
                   OR LOWER(l.nameEn) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%'))
                   OR LOWER(l.code) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%'))
                   OR LOWER(l.about) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')))
            """)
    Page<Lecture> searchMatching(@Param("term") Integer term,
                                 @Param("semester") Semester semester,
                                 @Param("degreeLevel") DegreeLevel degreeLevel,
                                 @Param("program") Program program,
                                 @Param("type") LectureType type,
                                 @Param("category") LectureCategory category,
                                 @Param("filterByLanguage") boolean filterByLanguage,
                                 @Param("languages") Collection<InstructionLanguage> languages,
                                 @Param("search") String search,
                                 Pageable pageable);

    // Dil süzgeci "verilenlerden herhangi biri"; hiç verilmezse dili boş dersler de düşmesin diye
    // bayrakla kapatılır, IN listesi hiçbir zaman boş bağlanmaz.
    default Page<Lecture> search(Integer term, Semester semester, DegreeLevel degreeLevel, Program program, LectureType type,
                                 LectureCategory category, Collection<InstructionLanguage> languages,
                                 String search, Pageable pageable) {
        var filterByLanguage = languages != null && !languages.isEmpty();
        return searchMatching(term, semester, degreeLevel, program, type, category, filterByLanguage,
                filterByLanguage ? EnumSet.copyOf(languages) : EnumSet.allOf(InstructionLanguage.class),
                search, pageable);
    }
}
