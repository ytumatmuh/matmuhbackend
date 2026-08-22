package com.matmuh.matmuhsite.dataAccess.abstracts;

import com.matmuh.matmuhsite.entities.CalendarEvent;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface CalendarEventDao extends JpaRepository<CalendarEvent, UUID> {

    @EntityGraph(attributePaths = {"lectureOffering", "lectureOffering.lecture", "lectureOffering.staff"})
    @Query("""
            SELECT e FROM CalendarEvent e
            WHERE e.startsAt < :to AND COALESCE(e.endsAt, e.startsAt) >= :from
            ORDER BY e.startsAt ASC
            """)
    List<CalendarEvent> findInRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // "Ders Programım": kayıtlı olunan açılışların sınavları + derse bağlı olmayan genel kayıtlar.
    @EntityGraph(attributePaths = {"lectureOffering", "lectureOffering.lecture", "lectureOffering.staff"})
    @Query("""
            SELECT e FROM CalendarEvent e
            WHERE e.startsAt < :to AND COALESCE(e.endsAt, e.startsAt) >= :from
              AND (e.lectureOffering IS NULL OR e.lectureOffering.id IN :offeringIds)
            ORDER BY e.startsAt ASC
            """)
    List<CalendarEvent> findInRangeForOfferings(@Param("from") LocalDateTime from,
                                                @Param("to") LocalDateTime to,
                                                @Param("offeringIds") Collection<UUID> offeringIds);
}
