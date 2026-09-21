package com.matmuh.matmuhsite.core.mappers;

import com.matmuh.matmuhsite.core.dtos.staff.OfficeHourDto;
import com.matmuh.matmuhsite.core.dtos.staff.request.CreateStaffRequestDto;
import com.matmuh.matmuhsite.core.dtos.staff.request.UpdateStaffRequestDto;
import com.matmuh.matmuhsite.entities.OfficeHour;
import com.matmuh.matmuhsite.entities.Staff;
import com.matmuh.matmuhsite.entities.StaffGroup;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaffMapperTest {

    private final StaffMapper mapper = Mappers.getMapper(StaffMapper.class);

    // Veritabanı alfabetik verir (FRIDAY, MONDAY, WEDNESDAY); yanıt haftalık sırada olmalı.
    @Test
    void officeHoursComeOutMondayFirstThenByStart() {
        var staff = new Staff();
        staff.setOfficeHours(new ArrayList<>(List.of(
                new OfficeHour(DayOfWeek.FRIDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), null),
                new OfficeHour(DayOfWeek.MONDAY, LocalTime.of(14, 0), LocalTime.of(15, 0), null),
                new OfficeHour(DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(12, 0), null),
                new OfficeHour(DayOfWeek.WEDNESDAY, LocalTime.of(13, 0), LocalTime.of(14, 30), null))));

        var hours = mapper.toStaffDto(staff).getOfficeHours();

        assertEquals(List.of("MONDAY 10:00", "MONDAY 14:00", "WEDNESDAY 13:00", "FRIDAY 09:00"),
                hours.stream().map(h -> h.getDayOfWeek() + " " + h.getStartTime()).toList());
    }

    @Test
    void createRequestRoundTripsOfficeHours() {
        var request = academicStaff("Ayşe", "Yılmaz");
        request.setOfficeHours(List.of(hour(DayOfWeek.MONDAY, "10:00", "12:00", "Ofis D-105")));

        var dto = mapper.toStaffDto(mapper.toStaff(request));
        var mapped = dto.getOfficeHours().get(0);

        assertEquals(1, dto.getOfficeHours().size());
        assertEquals(DayOfWeek.MONDAY, mapped.getDayOfWeek());
        assertEquals(LocalTime.of(10, 0), mapped.getStartTime());
        assertEquals(LocalTime.of(12, 0), mapped.getEndTime());
        assertEquals("Ofis D-105", mapped.getDescription());
    }

    @Test
    void updateReplacesOfficeHoursWhenProvided() {
        var staff = new Staff();
        staff.setOfficeHours(new ArrayList<>(List.of(
                new OfficeHour(DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), "eski"))));

        var update = new UpdateStaffRequestDto();
        update.setOfficeHours(List.of(hour(DayOfWeek.WEDNESDAY, "14:00", "16:00", "yeni")));
        mapper.updateStaffFromDto(update, staff);

        assertEquals(1, staff.getOfficeHours().size());
        assertEquals(DayOfWeek.WEDNESDAY, staff.getOfficeHours().get(0).getDayOfWeek());
        assertEquals("yeni", staff.getOfficeHours().get(0).getDescription());
    }

    @Test
    void updateLeavesOfficeHoursWhenOmitted() {
        var staff = new Staff();
        staff.setOfficeHours(new ArrayList<>(List.of(
                new OfficeHour(DayOfWeek.FRIDAY, LocalTime.of(11, 0), LocalTime.of(12, 0), "korunur"))));

        mapper.updateStaffFromDto(new UpdateStaffRequestDto(), staff);

        assertEquals(1, staff.getOfficeHours().size());
        assertEquals("korunur", staff.getOfficeHours().get(0).getDescription());
    }

    @Test
    void updateClearsOfficeHoursWhenEmptyListIsSent() {
        var staff = new Staff();
        staff.setOfficeHours(new ArrayList<>(List.of(
                new OfficeHour(DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(12, 0), null))));

        var update = new UpdateStaffRequestDto();
        update.setOfficeHours(List.of());
        mapper.updateStaffFromDto(update, staff);

        assertTrue(staff.getOfficeHours().isEmpty());
    }

    private static CreateStaffRequestDto academicStaff(String firstName, String lastName) {
        var request = new CreateStaffRequestDto();
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setGroups(Set.of(StaffGroup.ACADEMIC));
        return request;
    }

    private static OfficeHourDto hour(DayOfWeek day, String start, String end, String description) {
        var hour = new OfficeHourDto();
        hour.setDayOfWeek(day);
        hour.setStartTime(LocalTime.parse(start));
        hour.setEndTime(LocalTime.parse(end));
        hour.setDescription(description);
        return hour;
    }
}
