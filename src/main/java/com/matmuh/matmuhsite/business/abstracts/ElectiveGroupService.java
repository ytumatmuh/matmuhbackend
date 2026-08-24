package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.dtos.common.PageDto;
import com.matmuh.matmuhsite.core.dtos.electiveGroup.request.CreateElectiveGroupRequestDto;
import com.matmuh.matmuhsite.core.dtos.electiveGroup.request.UpdateElectiveGroupRequestDto;
import com.matmuh.matmuhsite.core.dtos.electiveGroup.response.ElectiveGroupDto;
import com.matmuh.matmuhsite.entities.ElectiveGroup;
import com.matmuh.matmuhsite.core.dtos.electiveGroup.response.ElectiveGroupOptionDto;
import com.matmuh.matmuhsite.entities.DegreeLevel;
import com.matmuh.matmuhsite.entities.Semester;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ElectiveGroupService {

    PageDto<ElectiveGroupDto> getElectiveGroups(Integer term, Semester semester, DegreeLevel degreeLevel,
                                                String search, Pageable pageable);

    ElectiveGroupDto getElectiveGroupById(UUID id);

    ElectiveGroupDto toDto(ElectiveGroup group);

    ElectiveGroupDto getElectiveGroupByCode(String code);

    List<ElectiveGroupOptionDto> getOptions(UUID id);

    List<ElectiveGroupDto> getGroupsContainingLecture(UUID lectureId);

    ElectiveGroupDto createElectiveGroup(CreateElectiveGroupRequestDto requestDto);

    ElectiveGroupDto updateElectiveGroup(UUID id, UpdateElectiveGroupRequestDto requestDto);

    ElectiveGroupDto addOption(UUID id, UUID lectureId);

    ElectiveGroupDto removeOption(UUID id, UUID lectureId);

    void deleteElectiveGroup(UUID id);
}
