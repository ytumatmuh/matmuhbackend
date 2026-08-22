package com.matmuh.matmuhsite.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "schedule_slots",
        indexes = @Index(name = "ix_schedule_slot_offering", columnList = "lecture_offering_id")
)
public class ScheduleSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lecture_offering_id", nullable = false)
    private LectureOffering lectureOffering;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", length = 12, nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "classroom", length = 60)
    private String classroom;

    @Column(name = "online", nullable = false)
    @Builder.Default
    private boolean online = false;
}
