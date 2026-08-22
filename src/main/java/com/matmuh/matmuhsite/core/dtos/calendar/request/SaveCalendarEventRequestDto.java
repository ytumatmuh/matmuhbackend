package com.matmuh.matmuhsite.core.dtos.calendar.request;

import com.matmuh.matmuhsite.entities.CalendarEventType;
import com.matmuh.matmuhsite.entities.ExamType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class SaveCalendarEventRequestDto {

    @NotNull(message = "{event.type.not.null}")
    private CalendarEventType type;

    private ExamType examType;
    private UUID lectureOfferingId;

    @NotBlank(message = "{event.title.not.blank}")
    private String title;

    private String description;

    @NotNull(message = "{event.start.not.null}")
    private LocalDateTime startsAt;

    private LocalDateTime endsAt;

    private boolean allDay;

    private String classroom;
}
