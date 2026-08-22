package com.matmuh.matmuhsite.core.dtos.calendar.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class SaveScheduleSlotRequestDto {

    @NotNull(message = "{slot.offering.not.null}")
    private UUID lectureOfferingId;

    @NotNull(message = "{slot.day.not.null}")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "{slot.start.not.null}")
    private LocalTime startTime;

    @NotNull(message = "{slot.end.not.null}")
    private LocalTime endTime;

    private String classroom;

    private boolean online;
}
