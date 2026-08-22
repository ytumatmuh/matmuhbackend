package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.dtos.common.PageDto;
import com.matmuh.matmuhsite.core.dtos.staff.request.CreateStaffRequestDto;
import org.springframework.data.domain.Pageable;
import com.matmuh.matmuhsite.core.dtos.staff.request.UpdateStaffRequestDto;
import com.matmuh.matmuhsite.core.dtos.staff.response.StaffDto;
import com.matmuh.matmuhsite.entities.Staff;
import com.matmuh.matmuhsite.entities.StaffGroup;

import java.util.List;
import java.util.UUID;

public interface StaffService {

    StaffDto getStaffById(UUID id);

    Staff getStaffReferenceById(UUID id);

    StaffDto createStaff(CreateStaffRequestDto createStaffRequestDto);


    StaffDto updateStaff(UUID id, UpdateStaffRequestDto updateStaffRequestDto);

    void deleteStaff(UUID id);

    PageDto<StaffDto> getStaff(String search, StaffGroup group, String academicTitle, Pageable pageable);

}
