package com.matmuh.matmuhsite.dataAccess.abstracts;

import com.matmuh.matmuhsite.entities.Lecture;
import com.matmuh.matmuhsite.entities.LectureNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LectureNoteDao extends JpaRepository<LectureNote, UUID> {
    List<LectureNote> findByLectureAndIsApproved(Lecture lecture, boolean approved);

    List<LectureNote> findByIsApproved(Boolean approved);
}
