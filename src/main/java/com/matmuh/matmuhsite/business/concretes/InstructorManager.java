package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.InstructorService;
import com.matmuh.matmuhsite.business.constants.InstructorMessages;
import com.matmuh.matmuhsite.core.dtos.instructor.request.CreateInstructorRequestDto;
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
    public List<InstructorDto> getAllInstructors() {
        logger.info("Fetching all instructors");

        List<Instructor> instructors = instructorDao.findAll();

        logger.info("Total instructors found: {}", instructors.size());

        return instructorMapper.toInstructorDtoList(instructors);
    }
}
