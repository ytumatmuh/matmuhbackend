package com.matmuh.matmuhsite.core.dtos.staff;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.matmuh.matmuhsite.business.constants.StaffMessages;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OfficeHourDto {

    @NotNull(message = StaffMessages.OFFICE_HOUR_DAY_NOT_NULL)
    private DayOfWeek dayOfWeek;

    @NotNull(message = StaffMessages.OFFICE_HOUR_START_NOT_NULL)
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @NotNull(message = StaffMessages.OFFICE_HOUR_END_NOT_NULL)
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    @Size(max = 255, message = StaffMessages.OFFICE_HOUR_DESCRIPTION_TOO_LONG)
    private String description;

    @Size(max = 255, message = StaffMessages.OFFICE_HOUR_DESCRIPTION_TOO_LONG)
    private String descriptionEn;

    @JsonIgnore
    @AssertTrue(message = StaffMessages.OFFICE_HOUR_TIME_INVALID)
    public boolean isTimeRangeOrdered() {
        return startTime == null || endTime == null || endTime.isAfter(startTime);
    }
}
