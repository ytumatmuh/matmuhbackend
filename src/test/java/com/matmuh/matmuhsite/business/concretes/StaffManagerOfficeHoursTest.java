package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.constants.StaffMessages;
import com.matmuh.matmuhsite.core.dtos.staff.OfficeHourDto;
import com.matmuh.matmuhsite.core.dtos.staff.request.CreateStaffRequestDto;
import com.matmuh.matmuhsite.core.dtos.staff.request.UpdateStaffRequestDto;
import com.matmuh.matmuhsite.core.exceptions.BusinessRuleException;
import com.matmuh.matmuhsite.core.mappers.StaffMapper;
import com.matmuh.matmuhsite.dataAccess.abstracts.StaffDao;
import com.matmuh.matmuhsite.entities.OfficeHour;
import com.matmuh.matmuhsite.entities.Staff;
import com.matmuh.matmuhsite.entities.StaffGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StaffManagerOfficeHoursTest {

    private StaffDao staffDao;
    private StaffManager staffManager;

    @BeforeEach
    void setUp() {
        staffDao = mock(StaffDao.class);
        when(staffDao.existsBySlug(any())).thenReturn(false);
        when(staffDao.save(any(Staff.class))).thenAnswer(invocation -> {
            Staff staff = invocation.getArgument(0);
            if (staff.getId() == null) {
                staff.setId(UUID.randomUUID());
            }
            return staff;
        });
        staffManager = new StaffManager(staffDao, Mappers.getMapper(StaffMapper.class));
    }

    @Test
    void createWithoutHoursReturnsEmptyList() {
        var created = staffManager.createStaff(academicStaff("Ayşe", "Yılmaz"));

        assertEquals(List.of(), created.getOfficeHours());
    }

    @Test
    void createPersistsOfficeHours() {
        var request = academicStaff("Ayşe", "Yılmaz");
        request.setOfficeHours(List.of(hour(DayOfWeek.MONDAY, "10:00", "12:00", "Ofis D-105")));

        var created = staffManager.createStaff(request);

        assertEquals(1, created.getOfficeHours().size());
        var hour = created.getOfficeHours().get(0);
        assertEquals(DayOfWeek.MONDAY, hour.getDayOfWeek());
        assertEquals(LocalTime.of(10, 0), hour.getStartTime());
        assertEquals(LocalTime.of(12, 0), hour.getEndTime());
        assertEquals("Ofis D-105", hour.getDescription());
    }

    @Test
    void getReturnsOfficeHours() {
        var id = UUID.randomUUID();
        var staff = new Staff();
        staff.setId(id);
        staff.setFirstName("Ayşe");
        staff.setLastName("Yılmaz");
        staff.setOfficeHours(new ArrayList<>(List.of(
                new OfficeHour(DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(12, 0), "Ofis D-105"))));
        when(staffDao.findById(id)).thenReturn(Optional.of(staff));

        var dto = staffManager.getStaffById(id);

        assertEquals(1, dto.getOfficeHours().size());
        assertEquals(DayOfWeek.MONDAY, dto.getOfficeHours().get(0).getDayOfWeek());
        assertEquals("Ofis D-105", dto.getOfficeHours().get(0).getDescription());
    }

    @Test
    void updateReplacesOfficeHours() {
        var id = UUID.randomUUID();
        var staff = new Staff();
        staff.setId(id);
        staff.setFirstName("Ayşe");
        staff.setLastName("Yılmaz");
        staff.setOfficeHours(new ArrayList<>(List.of(
                new OfficeHour(DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), "eski"))));
        when(staffDao.findById(id)).thenReturn(Optional.of(staff));

        var update = new UpdateStaffRequestDto();
        update.setOfficeHours(List.of(hour(DayOfWeek.WEDNESDAY, "14:00", "16:00", "yeni")));

        var updated = staffManager.updateStaff(id, update);

        assertEquals(DayOfWeek.WEDNESDAY, updated.getOfficeHours().get(0).getDayOfWeek());
        assertEquals("yeni", updated.getOfficeHours().get(0).getDescription());
    }

    @Test
    void createRejectsStartAtOrAfterEnd() {
        var request = academicStaff("Ayşe", "Yılmaz");
        request.setOfficeHours(List.of(hour(DayOfWeek.MONDAY, "12:00", "10:00", null)));

        var ex = assertThrows(BusinessRuleException.class, () -> staffManager.createStaff(request));
        assertEquals(StaffMessages.OFFICE_HOUR_TIME_INVALID, ex.getMessage());
    }

    @Test
    void updateRejectsEqualStartAndEnd() {
        var id = UUID.randomUUID();
        var staff = new Staff();
        staff.setId(id);
        staff.setFirstName("Ayşe");
        staff.setLastName("Yılmaz");
        when(staffDao.findById(id)).thenReturn(Optional.of(staff));

        var update = new UpdateStaffRequestDto();
        update.setOfficeHours(List.of(hour(DayOfWeek.MONDAY, "10:00", "10:00", null)));

        var ex = assertThrows(BusinessRuleException.class, () -> staffManager.updateStaff(id, update));
        assertEquals(StaffMessages.OFFICE_HOUR_TIME_INVALID, ex.getMessage());
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
