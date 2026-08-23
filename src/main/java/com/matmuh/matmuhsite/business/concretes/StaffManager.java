package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.StaffService;
import com.matmuh.matmuhsite.business.constants.StaffMessages;
import com.matmuh.matmuhsite.core.dtos.common.PageDto;
import com.matmuh.matmuhsite.core.dtos.staff.request.CreateStaffRequestDto;
import com.matmuh.matmuhsite.core.helpers.UniqueSlugResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import com.matmuh.matmuhsite.core.dtos.staff.response.StaffDto;
import com.matmuh.matmuhsite.core.exceptions.ResourceNotFoundException;
import com.matmuh.matmuhsite.core.mappers.StaffMapper;
import com.matmuh.matmuhsite.dataAccess.abstracts.StaffDao;
import com.matmuh.matmuhsite.entities.Staff;
import com.matmuh.matmuhsite.entities.StaffGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class StaffManager implements StaffService {

    private final StaffDao staffDao;
    private final StaffMapper staffMapper;

    public StaffManager(StaffDao staffDao, StaffMapper staffMapper) {
        this.staffDao = staffDao;
        this.staffMapper = staffMapper;
    }

    @Override
    public StaffDto getStaffById(UUID id) {
        log.info("Fetching staff with ID: {}", id);

        Staff staff  = staffDao.findById(id).orElseThrow(()->{
            log.error("Staff not found with ID: {}", id);
            return new ResourceNotFoundException(StaffMessages.STAFF_NOT_FOUND);
        });

        log.info("Staff found: {}", staff.getFirstName() + " " + staff.getLastName());

        return staffMapper.toStaffDto(staff);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public StaffDto updateStaff(UUID id, com.matmuh.matmuhsite.core.dtos.staff.request.UpdateStaffRequestDto request) {
        log.info("Updating staff with ID: {}", id);

        Staff staff = staffDao.findById(id).orElseThrow(() -> {
            log.error("Staff not found with ID: {}", id);
            return new ResourceNotFoundException(StaffMessages.STAFF_NOT_FOUND);
        });

        staffMapper.updateStaffFromDto(request, staff);
        applyPhotoAlt(staff);
        var saved = staffDao.save(staff);

        log.info("Staff updated with ID: {}", saved.getId());
        return staffMapper.toStaffDto(saved);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void deleteStaff(UUID id) {
        log.info("Deleting staff with ID: {}", id);

        Staff staff = staffDao.findById(id).orElseThrow(() -> {
            log.error("Staff not found with ID: {}", id);
            return new ResourceNotFoundException(StaffMessages.STAFF_NOT_FOUND);
        });

        staffDao.delete(staff);
        log.info("Staff soft deleted with ID: {}", id);
    }

    @Override
    public Staff getStaffReferenceById(UUID id) {
        log.info("Fetching staff reference with ID: {}", id);

        return staffDao.getReferenceById(id);

    }

    @Override
    public StaffDto createStaff(CreateStaffRequestDto createStaffRequestDto) {
        log.info("Creating new staff with email: {}", createStaffRequestDto.getEmail());

        Staff staff = staffMapper.toStaff(createStaffRequestDto);

        staff.setSlug(UniqueSlugResolver.resolve(
                createStaffRequestDto.getSlug(),
                createStaffRequestDto.getFirstName()+ " " + createStaffRequestDto.getLastName(),
                createStaffRequestDto.getLastName(),
                staffDao::existsBySlug,
                StaffMessages.SLUG_INVALID,
                StaffMessages.SLUG_EXISTS
        ));

        applyPhotoAlt(staff);

        Staff savedStaff = staffDao.save(staff);

        log.info("Staff created with ID: {}", savedStaff.getId());

        return staffMapper.toStaffDto(savedStaff);
    }

    @Override
    public PageDto<StaffDto> getStaff(String search, StaffGroup group, String academicTitle, Pageable pageable) {
        log.info("Fetching staff search={} group={} academicTitle={} page={}", search, group, academicTitle, pageable.getPageNumber());

        var page = staffDao.search(normalize(search), group, normalize(academicTitle), pageable);

        log.info("Total staff found: {}", page.getTotalElements());

        return PageDto.of(page, staffMapper::toStaffDto);
    }

    // Alt metni erisilebilirlik icin gerekli; REST/ice aktarma yolunda gonderilmezse
    // kisinin adindan doldurulur. CMS tarafinda SDK zaten alt kutusu ciziyor.
    private void applyPhotoAlt(Staff staff) {
        var photo = staff.getPhoto();
        if (photo == null || photo.getSrc() == null || photo.getSrc().isBlank()) {
            staff.setPhoto(null);
            return;
        }
        if (photo.getAlt() == null || photo.getAlt().isBlank()) {
            photo.setAlt((staff.getFirstName() + " " + staff.getLastName()).trim());
        }
    }

    private String normalize(String search) {
        return search == null || search.isBlank() ? null : search.trim();
    }
}
