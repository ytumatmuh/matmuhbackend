package com.matmuh.matmuhsite.dataAccess.abstracts;

import com.matmuh.matmuhsite.entities.ScheduleSlot;
import com.matmuh.matmuhsite.entities.Semester;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface ScheduleSlotDao extends JpaRepository<ScheduleSlot, UUID> {

    @EntityGraph(attributePaths = {"lectureOffering", "lectureOffering.lecture", "lectureOffering.staff"})
    List<ScheduleSlot> findByLectureOfferingId(UUID lectureOfferingId);

    @EntityGraph(attributePaths = {"lectureOffering", "lectureOffering.lecture", "lectureOffering.staff"})
    @Query("""
            SELECT s FROM ScheduleSlot s
            WHERE s.lectureOffering.academicYear = :academicYear
              AND s.lectureOffering.semester = :semester
            """)
    List<ScheduleSlot> findByTerm(@Param("academicYear") String academicYear,
                                  @Param("semester") Semester semester);

    @EntityGraph(attributePaths = {"lectureOffering", "lectureOffering.lecture", "lectureOffering.staff"})
    List<ScheduleSlot> findByLectureOfferingIdIn(Collection<UUID> offeringIds);
}
