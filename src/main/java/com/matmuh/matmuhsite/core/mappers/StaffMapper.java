package com.matmuh.matmuhsite.core.mappers;

import com.matmuh.matmuhsite.core.dtos.staff.OfficeHourDto;
import com.matmuh.matmuhsite.core.dtos.staff.request.CreateStaffRequestDto;
import com.matmuh.matmuhsite.core.dtos.staff.request.UpdateStaffRequestDto;
import com.matmuh.matmuhsite.core.dtos.staff.response.StaffDto;
import com.matmuh.matmuhsite.entities.OfficeHour;
import com.matmuh.matmuhsite.entities.Staff;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring")
public interface StaffMapper {

    StaffDto toStaffDto(Staff staff);

    Staff toStaff(CreateStaffRequestDto createStaffRequestDto);

    List<StaffDto> toStaffDtoList(List<Staff> staff);

    OfficeHour toOfficeHour(OfficeHourDto dto);

    OfficeHourDto toOfficeHourDto(OfficeHour hour);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "offerings", ignore = true)
    @Mapping(target = "officeHours", ignore = true)
    void updateStaffFromDto(UpdateStaffRequestDto dto, @MappingTarget Staff staff);

    @AfterMapping
    default void emptyOfficeHours(@MappingTarget Staff staff) {
        if (staff.getOfficeHours() == null) {
            staff.setOfficeHours(new ArrayList<>());
        }
    }

    // Kolon STRING enum olduğu için @OrderBy alfabetik sıralar (FRIDAY < MONDAY); haftalık
    // sıra burada kurulur ki her tüketici (REST, CMS) aynı düzeni alsın.
    @AfterMapping
    default void orderOfficeHours(@MappingTarget StaffDto dto) {
        if (dto.getOfficeHours() == null) {
            dto.setOfficeHours(new ArrayList<>());
            return;
        }
        var ordered = new ArrayList<>(dto.getOfficeHours());
        ordered.sort(Comparator.comparing(OfficeHourDto::getDayOfWeek).thenComparing(OfficeHourDto::getStartTime));
        dto.setOfficeHours(ordered);
    }

    @AfterMapping
    default void replaceOfficeHours(UpdateStaffRequestDto dto, @MappingTarget Staff staff) {
        if (dto.getOfficeHours() == null) {
            return;
        }
        var hours = staff.getOfficeHours();
        if (hours == null) {
            hours = new ArrayList<>();
            staff.setOfficeHours(hours);
        } else {
            hours.clear();
        }
        for (var item : dto.getOfficeHours()) {
            hours.add(toOfficeHour(item));
        }
    }
}
