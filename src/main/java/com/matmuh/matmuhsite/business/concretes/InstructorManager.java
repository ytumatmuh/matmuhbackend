package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.InstructorService;
import com.matmuh.matmuhsite.business.constants.InstructorMessages;
import com.matmuh.matmuhsite.core.dtos.common.PageDto;
import com.matmuh.matmuhsite.core.dtos.instructor.request.CreateInstructorRequestDto;
import org.springframework.data.domain.Pageable;
import com.matmuh.matmuhsite.core.dtos.instructor.response.InstructorDto;
import com.matmuh.matmuhsite.core.exceptions.ResourceNotFoundException;
import com.matmuh.matmuhsite.core.mappers.InstructorMapper;
import com.matmuh.matmuhsite.dataAccess.abstracts.InstructorDao;
import com.matmuh.matmuhsite.entities.Instructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class InstructorManager implements InstructorService {

    private final Logger logger = LoggerFactory.getLogger(InstructorManager.class);

    private final InstructorDao instructorDao;

    private final InstructorMapper instructorMapper;

    public InstructorManager(InstructorDao instructorDao, InstructorMapper instructorMapper) {
        this.instructorDao = instructorDao;
        this.instructorMapper = instructorMapper;
    }

    @Override
    public InstructorDto getInstructorById(UUID id) {
        logger.info("Fetching instructor with ID: {}", id);

        Instructor instructor  = instructorDao.findById(id).orElseThrow(()->{
            logger.error("Instructor not found with ID: {}", id);
            return new ResourceNotFoundException(InstructorMessages.INSTRUCTOR_NOT_FOUND);
        });

        logger.info("Instructor found: {}", instructor.getFirstName() + " " + instructor.getLastName());

        return instructorMapper.toInstructorDto(instructor);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public InstructorDto updateInstructor(UUID id, com.matmuh.matmuhsite.core.dtos.instructor.request.UpdateInstructorRequestDto request) {
        logger.info("Updating instructor with ID: {}", id);

        Instructor instructor = instructorDao.findById(id).orElseThrow(() -> {
            logger.error("Instructor not found with ID: {}", id);
            return new ResourceNotFoundException(InstructorMessages.INSTRUCTOR_NOT_FOUND);
        });

        instructorMapper.updateInstructorFromDto(request, instructor);
        var saved = instructorDao.save(instructor);

        logger.info("Instructor updated with ID: {}", saved.getId());
        return instructorMapper.toInstructorDto(saved);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void deleteInstructor(UUID id) {
        logger.info("Deleting instructor with ID: {}", id);

        Instructor instructor = instructorDao.findById(id).orElseThrow(() -> {
            logger.error("Instructor not found with ID: {}", id);
            return new ResourceNotFoundException(InstructorMessages.INSTRUCTOR_NOT_FOUND);
        });

        instructorDao.delete(instructor);
        logger.info("Instructor soft deleted with ID: {}", id);
    }

    @Override
    public Instructor getInstructorReferenceById(UUID id) {
        logger.info("Fetching instructor reference with ID: {}", id);

        return instructorDao.getReferenceById(id);

    }

    @Override
    public InstructorDto createInstructor(CreateInstructorRequestDto createInstructorRequestDto) {
        logger.info("Creating new instructor with email: {}", createInstructorRequestDto.getEmail());

        Instructor instructor = instructorMapper.toInstructor(createInstructorRequestDto);

        Instructor savedInstructor = instructorDao.save(instructor);

        logger.info("Instructor created with ID: {}", savedInstructor.getId());

        return instructorMapper.toInstructorDto(savedInstructor);
    }

    @Override
    public PageDto<InstructorDto> getInstructors(String search, Pageable pageable) {
        logger.info("Fetching instructors search={} page={}", search, pageable.getPageNumber());

        var page = instructorDao.search(normalize(search), pageable);

        logger.info("Total instructors found: {}", page.getTotalElements());

        return PageDto.of(page, instructorMapper::toInstructorDto);
    }

    private String normalize(String search) {
        return search == null || search.isBlank() ? null : search.trim();
    }
}
