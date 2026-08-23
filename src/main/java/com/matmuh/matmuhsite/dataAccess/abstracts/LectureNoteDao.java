package com.matmuh.matmuhsite.dataAccess.abstracts;

import com.matmuh.matmuhsite.entities.Lecture;
import com.matmuh.matmuhsite.entities.LectureNote;
import com.matmuh.matmuhsite.entities.NoteReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface LectureNoteDao extends JpaRepository<LectureNote, UUID> {

    List<LectureNote> findByLectureAndStatus(Lecture lecture, NoteReviewStatus status);

    @Query("""
            SELECT n.lecture.id, COUNT(n)
            FROM LectureNote n
            WHERE n.status = com.matmuh.matmuhsite.entities.NoteReviewStatus.APPROVED
              AND n.lecture.id IN :lectureIds
            GROUP BY n.lecture.id
            """)
    List<Object[]> countApprovedByLectureIds(@Param("lectureIds") Collection<UUID> lectureIds);

    Page<LectureNote> findByLectureAndStatus(Lecture lecture, NoteReviewStatus status, Pageable pageable);

    @Query("""
            SELECT n FROM LectureNote n
            LEFT JOIN n.lectureOffering o
            LEFT JOIN o.staff i
            WHERE (:status IS NULL OR n.status = :status)
              AND (:lectureId IS NULL OR n.lecture.id = :lectureId)
              AND (:offeringId IS NULL OR o.id = :offeringId)
              AND (:staffId IS NULL OR i.id = :staffId)
              AND (:uploaderId IS NULL OR n.createdBy.id = :uploaderId)
              AND (CAST(:search AS String) IS NULL
                   OR LOWER(n.title) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%'))
                   OR LOWER(n.description) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')))
            """)
    Page<LectureNote> search(@Param("status") NoteReviewStatus status,
                             @Param("lectureId") UUID lectureId,
                             @Param("offeringId") UUID offeringId,
                             @Param("staffId") UUID staffId,
                             @Param("uploaderId") UUID uploaderId,
                             @Param("search") String search,
                             Pageable pageable);
}
