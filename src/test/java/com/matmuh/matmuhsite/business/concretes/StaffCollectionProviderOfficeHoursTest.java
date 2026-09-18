package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.StaffService;
import com.matmuh.matmuhsite.core.dtos.staff.request.CreateStaffRequestDto;
import com.matmuh.matmuhsite.core.dtos.staff.request.UpdateStaffRequestDto;
import com.matmuh.matmuhsite.core.dtos.staff.response.StaffDto;
import com.matmuh.matmuhsite.core.exceptions.CmsValidationException;
import com.matmuh.matmuhsite.core.mappers.StaffMapper;
import com.matmuh.matmuhsite.dataAccess.abstracts.StaffDao;
import com.matmuh.matmuhsite.entities.OfficeHour;
import com.matmuh.matmuhsite.entities.Staff;
import com.matmuh.matmuhsite.entities.StaffGroup;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StaffCollectionProviderOfficeHoursTest {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private StaffService staffService;
    private StaffDao staffDao;
    private StaffCollectionProvider provider;

    @BeforeEach
    void setUp() {
        staffService = mock(StaffService.class);
        staffDao = mock(StaffDao.class);
        provider = new StaffCollectionProvider(
                staffService,
                staffDao,
                Mappers.getMapper(StaffMapper.class),
                MAPPER,
                Validation.buildDefaultValidatorFactory().getValidator());
    }

    @Test
    void createMapsOfficeHoursIntoTheStaffRequest() {
        when(staffService.createStaff(any())).thenAnswer(invocation -> {
            CreateStaffRequestDto request = invocation.getArgument(0);
            var dto = new StaffDto();
            dto.setId(UUID.randomUUID());
            dto.setFirstName(request.getFirstName());
            dto.setLastName(request.getLastName());
            dto.setSlug("yilmaz");
            dto.setOfficeHours(request.getOfficeHours());
            return dto;
        });
        when(staffDao.findBySlug("yilmaz")).thenReturn(Optional.empty());

        var item = provider.create(object("""
                {
                  "firstName": "Ayşe",
                  "lastName": "Yılmaz",
                  "groups": ["ACADEMIC"],
                  "officeHours": [
                    {"dayOfWeek": "MONDAY", "startTime": "10:00", "endTime": "12:00", "description": "Ofis D-105"}
                  ]
                }
                """), null);

        var captor = ArgumentCaptor.forClass(CreateStaffRequestDto.class);
        verify(staffService).createStaff(captor.capture());
        var hour = captor.getValue().getOfficeHours().get(0);
        assertEquals(DayOfWeek.MONDAY, hour.getDayOfWeek());
        assertEquals(LocalTime.of(10, 0), hour.getStartTime());
        assertEquals(LocalTime.of(12, 0), hour.getEndTime());
        assertEquals("Ofis D-105", hour.getDescription());

        var json = item.getData().toString();
        assertTrue(json.contains("\"startTime\":\"10:00\""), json);
        assertTrue(json.contains("MONDAY"), json);
    }

    @Test
    void getBySlugMapsOfficeHoursOut() {
        var staff = new Staff();
        staff.setId(UUID.randomUUID());
        staff.setFirstName("Ayşe");
        staff.setLastName("Yılmaz");
        staff.setSlug("yilmaz");
        staff.setGroups(Set.of(StaffGroup.ACADEMIC));
        staff.setOfficeHours(new ArrayList<>(List.of(
                new OfficeHour(DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(12, 0), "Ofis D-105"))));
        when(staffDao.findBySlug("yilmaz")).thenReturn(Optional.of(staff));

        var item = provider.getBySlug("yilmaz", null);

        var hours = item.getData().get("officeHours");
        assertEquals(1, hours.size());
        assertEquals("MONDAY", hours.get(0).get("dayOfWeek").asString());
        assertEquals("10:00", hours.get(0).get("startTime").asString());
        assertEquals("12:00", hours.get(0).get("endTime").asString());
        assertEquals("Ofis D-105", hours.get(0).get("description").asString());
    }

    @Test
    void upsertMapsOfficeHoursIntoTheUpdateRequest() {
        var staff = new Staff();
        staff.setId(UUID.randomUUID());
        staff.setSlug("yilmaz");
        staff.setVersion(3);
        when(staffDao.findBySlug("yilmaz")).thenReturn(Optional.of(staff));
        when(staffService.updateStaff(any(), any())).thenAnswer(invocation -> {
            UpdateStaffRequestDto request = invocation.getArgument(1);
            var dto = new StaffDto();
            dto.setId(staff.getId());
            dto.setSlug("yilmaz");
            dto.setOfficeHours(request.getOfficeHours());
            return dto;
        });

        provider.upsert("yilmaz", object("""
                {
                  "officeHours": [
                    {"dayOfWeek": "FRIDAY", "startTime": "13:00", "endTime": "14:30", "description": "randevu"}
                  ]
                }
                """), 3, null);

        var captor = ArgumentCaptor.forClass(UpdateStaffRequestDto.class);
        verify(staffService).updateStaff(any(), captor.capture());
        var hour = captor.getValue().getOfficeHours().get(0);
        assertEquals(DayOfWeek.FRIDAY, hour.getDayOfWeek());
        assertEquals(LocalTime.of(13, 0), hour.getStartTime());
        assertEquals(LocalTime.of(14, 30), hour.getEndTime());
        assertEquals("randevu", hour.getDescription());
    }

    @Test
    void createMapsEmptyOfficeHoursAsAnEmptyList() {
        when(staffService.createStaff(any())).thenAnswer(invocation -> {
            CreateStaffRequestDto request = invocation.getArgument(0);
            var dto = new StaffDto();
            dto.setId(UUID.randomUUID());
            dto.setFirstName(request.getFirstName());
            dto.setLastName(request.getLastName());
            dto.setSlug("yilmaz");
            dto.setOfficeHours(request.getOfficeHours());
            return dto;
        });
        when(staffDao.findBySlug("yilmaz")).thenReturn(Optional.empty());

        var item = provider.create(object("""
                {
                  "firstName": "Ayşe",
                  "lastName": "Yılmaz",
                  "groups": ["ACADEMIC"]
                }
                """), null);

        var captor = ArgumentCaptor.forClass(CreateStaffRequestDto.class);
        verify(staffService).createStaff(captor.capture());
        assertEquals(List.of(), captor.getValue().getOfficeHours());
        assertEquals(0, item.getData().get("officeHours").size());
    }

    @Test
    void createRejectsOfficeHoursThatEndBeforeTheyStart() {
        var ex = assertThrows(CmsValidationException.class, () -> provider.create(object("""
                {
                  "firstName": "Ayşe",
                  "lastName": "Yılmaz",
                  "groups": ["ACADEMIC"],
                  "officeHours": [
                    {"dayOfWeek": "MONDAY", "startTime": "12:00", "endTime": "10:00"}
                  ]
                }
                """), null));
        assertTrue(ex.getErrors().stream().anyMatch(error -> error.contains("officeHours")), ex.getErrors().toString());
    }

    @Test
    void createRejectsMissingDay() {
        assertThrows(CmsValidationException.class, () -> provider.create(object("""
                {
                  "firstName": "Ayşe",
                  "lastName": "Yılmaz",
                  "groups": ["ACADEMIC"],
                  "officeHours": [
                    {"startTime": "10:00", "endTime": "12:00"}
                  ]
                }
                """), null));
    }

    @Test
    void createRejectsBadTime() {
        assertThrows(CmsValidationException.class, () -> provider.create(object("""
                {
                  "firstName": "Ayşe",
                  "lastName": "Yılmaz",
                  "groups": ["ACADEMIC"],
                  "officeHours": [
                    {"dayOfWeek": "MONDAY", "startTime": "not-a-time", "endTime": "12:00"}
                  ]
                }
                """), null));
    }

    private static ObjectNode object(String json) {
        return (ObjectNode) MAPPER.readTree(json);
    }
}
