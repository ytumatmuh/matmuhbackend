package com.matmuh.matmuhsite.core.mappers;

import com.matmuh.matmuhsite.core.dtos.staff.request.CreateStaffRequestDto;
import com.matmuh.matmuhsite.core.dtos.staff.request.UpdateStaffRequestDto;
import com.matmuh.matmuhsite.core.dtos.staff.response.StaffDto;
import com.matmuh.matmuhsite.entities.Staff;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StaffMapper {

    StaffDto toStaffDto(Staff staff);

    Staff toStaff(CreateStaffRequestDto createStaffRequestDto);

    List<StaffDto> toStaffDtoList(List<Staff> staff);

    @org.mapstruct.BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @org.mapstruct.Mapping(target = "id", ignore = true)
    @org.mapstruct.Mapping(target = "offerings", ignore = true)
    void updateStaffFromDto(UpdateStaffRequestDto dto, @org.mapstruct.MappingTarget Staff staff);
}
