package com.matmuh.matmuhsite.dataAccess.abstracts;

import com.matmuh.matmuhsite.entities.ScheduleSlot;
import com.matmuh.matmuhsite.entities.Semester;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
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


    @EntityGraph(attributePaths = {"lectureOffering", "lectureOffering.lecture", "lectureOffering.staff"})
    @Query("""
            SELECT s FROM ScheduleSlot s
            WHERE s.lectureOffering.academicYear = :academicYear
              AND s.lectureOffering.semester = :semester
              AND s.dayOfWeek = :dayOfWeek
              AND s.startTime < :endTime
              AND s.endTime > :startTime
              AND (:excludedId IS NULL OR s.id <> :excludedId)
            """)
    List<ScheduleSlot> findOverlapping(@Param("academicYear") String academicYear,
                                       @Param("semester") Semester semester,
                                       @Param("dayOfWeek") DayOfWeek dayOfWeek,
                                       @Param("startTime") LocalTime startTime,
                                       @Param("endTime") LocalTime endTime,
                                       @Param("excludedId") UUID excludedId);
}
