package com.matmuh.matmuhsite.core.dtos.staff;

import com.matmuh.matmuhsite.core.dtos.staff.request.CreateStaffRequestDto;
import com.matmuh.matmuhsite.entities.StaffGroup;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OfficeHourDtoValidationTest {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void missingDayIsInvalid() {
        var hour = new OfficeHourDto();
        hour.setStartTime(LocalTime.of(10, 0));
        hour.setEndTime(LocalTime.of(12, 0));

        assertFalse(VALIDATOR.validate(hour).isEmpty());
    }

    @Test
    void restJsonUsesHourMinuteWithoutSeconds() {
        var hour = MAPPER.readValue("""
                {"dayOfWeek":"MONDAY","startTime":"10:00","endTime":"12:00","description":"Ofis D-105"}
                """, OfficeHourDto.class);

        assertEquals(DayOfWeek.MONDAY, hour.getDayOfWeek());
        assertEquals(LocalTime.of(10, 0), hour.getStartTime());
        assertEquals(LocalTime.of(12, 0), hour.getEndTime());

        var json = MAPPER.writeValueAsString(hour);
        assertTrue(json.contains("\"startTime\":\"10:00\""), json);
        assertTrue(json.contains("\"endTime\":\"12:00\""), json);
        assertFalse(json.contains("10:00:00"), json);
    }

    @Test
    void badTimeIsRejected() {
        assertThrows(JacksonException.class, () -> MAPPER.readValue("""
                {"firstName":"Ayşe","lastName":"Yılmaz","groups":["ACADEMIC"],
                 "officeHours":[{"dayOfWeek":"MONDAY","startTime":"not-a-time","endTime":"12:00"}]}
                """, CreateStaffRequestDto.class));
    }

    @Test
    void createRequestRequiresDayInsideOfficeHours() {
        var hour = new OfficeHourDto();
        hour.setStartTime(LocalTime.of(10, 0));
        hour.setEndTime(LocalTime.of(12, 0));

        var request = new CreateStaffRequestDto();
        request.setFirstName("Ayşe");
        request.setLastName("Yılmaz");
        request.setGroups(Set.of(StaffGroup.ACADEMIC));
        request.setOfficeHours(java.util.List.of(hour));

        var paths = VALIDATOR.validate(request).stream()
                .map(v -> v.getPropertyPath().toString())
                .toList();
        assertTrue(paths.stream().anyMatch(path -> path.contains("officeHours") && path.contains("dayOfWeek")),
                paths.toString());
    }
}
