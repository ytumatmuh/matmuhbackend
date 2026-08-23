package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.ElectiveGroupService;
import com.matmuh.matmuhsite.business.constants.ElectiveGroupMessages;
import com.matmuh.matmuhsite.business.constants.LectureMessages;
import com.matmuh.matmuhsite.core.dtos.common.PageDto;
import com.matmuh.matmuhsite.core.dtos.electiveGroup.request.CreateElectiveGroupRequestDto;
import com.matmuh.matmuhsite.core.dtos.electiveGroup.request.UpdateElectiveGroupRequestDto;
import com.matmuh.matmuhsite.core.dtos.electiveGroup.response.ElectiveGroupDto;
import com.matmuh.matmuhsite.core.dtos.electiveGroup.response.ElectiveGroupOptionDto;
import com.matmuh.matmuhsite.core.exceptions.ResourceAlreadyExistsException;
import com.matmuh.matmuhsite.core.exceptions.ResourceNotFoundException;
import com.matmuh.matmuhsite.core.helpers.UniqueSlugResolver;
import com.matmuh.matmuhsite.core.mappers.ElectiveGroupMapper;
import com.matmuh.matmuhsite.dataAccess.abstracts.ElectiveGroupDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.LectureDao;
import com.matmuh.matmuhsite.entities.DegreeLevel;
import com.matmuh.matmuhsite.entities.ElectiveGroup;
import com.matmuh.matmuhsite.entities.Lecture;
import com.matmuh.matmuhsite.entities.Semester;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class ElectiveGroupManager implements ElectiveGroupService {

    private final ElectiveGroupDao electiveGroupDao;
    private final LectureDao lectureDao;
    private final ElectiveGroupMapper electiveGroupMapper;

    public ElectiveGroupManager(ElectiveGroupDao electiveGroupDao,
                                LectureDao lectureDao,
                                ElectiveGroupMapper electiveGroupMapper) {
        this.electiveGroupDao = electiveGroupDao;
        this.lectureDao = lectureDao;
        this.electiveGroupMapper = electiveGroupMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public PageDto<ElectiveGroupDto> getElectiveGroups(Integer term, Semester semester, DegreeLevel degreeLevel,
                                                       String search, Pageable pageable) {
        log.info("Retrieving elective groups term={} semester={} degreeLevel={} search={} page={}",
                term, semester, degreeLevel, search, pageable.getPageNumber());

        var page = electiveGroupDao.search(term, semester, degreeLevel, normalize(search), pageable);

        return PageDto.of(page, this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ElectiveGroupDto getElectiveGroupById(UUID id) {
        log.info("Retrieving elective group with ID: {}", id);
        return toDto(require(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ElectiveGroupDto getElectiveGroupByCode(String code) {
        log.info("Retrieving elective group with code: {}", code);

        var group = electiveGroupDao.findByCodeIgnoreCase(code).orElseThrow(() -> {
            log.error("Elective group with code {} not found.", code);
            return new ResourceNotFoundException(ElectiveGroupMessages.ELECTIVE_GROUP_NOT_FOUND);
        });

        return toDto(group);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ElectiveGroupOptionDto> getOptions(UUID id) {
        log.info("Retrieving options of elective group with ID: {}", id);
        return sortedOptions(require(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ElectiveGroupDto> getGroupsContainingLecture(UUID lectureId) {
        log.info("Retrieving elective groups containing lecture with ID: {}", lectureId);

        if (!lectureDao.existsById(lectureId)) {
            log.error("Lecture with ID {} not found.", lectureId);
            throw new ResourceNotFoundException(LectureMessages.LECTURE_NOT_FOUND);
        }

        return electiveGroupDao.findByOptionLectureId(lectureId).stream().map(this::toDto).toList();
    }

    @Override
    @Transactional
    public ElectiveGroupDto createElectiveGroup(CreateElectiveGroupRequestDto requestDto) {
        log.info("Creating elective group with code: {}", requestDto.getCode());

        if (electiveGroupDao.existsByCodeIgnoreCase(requestDto.getCode())) {
            log.error("Elective group creation failed: code {} already exists.", requestDto.getCode());
            throw new ResourceAlreadyExistsException(ElectiveGroupMessages.CODE_EXISTS);
        }

        var group = electiveGroupMapper.toEntity(requestDto);
        group.setOptions(resolveLectures(requestDto.getOptionLectureIds()));

        if (group.getDegreeLevels() == null || group.getDegreeLevels().isEmpty()) {
            group.setDegreeLevels(deriveDegreeLevels(group.getOptions()));
        }

        if (group.getSelectionCount() < 1) {
            group.setSelectionCount(1);
        }

        group.setSlug(UniqueSlugResolver.resolve(
                requestDto.getSlug(),
                requestDto.getCode(),
                requestDto.getName(),
                electiveGroupDao::existsBySlug,
                ElectiveGroupMessages.SLUG_INVALID,
                ElectiveGroupMessages.SLUG_EXISTS));

        var saved = electiveGroupDao.save(group);
        log.info("Elective group created with ID: {}", saved.getId());

        return toDto(saved);
    }

    @Override
    @Transactional
    public ElectiveGroupDto updateElectiveGroup(UUID id, UpdateElectiveGroupRequestDto requestDto) {
        log.info("Updating elective group with ID: {}", id);

        var group = require(id);

        if (requestDto.getCode() != null && !requestDto.getCode().equalsIgnoreCase(group.getCode())
                && electiveGroupDao.existsByCodeIgnoreCase(requestDto.getCode())) {
            log.error("Elective group update failed: code {} already exists.", requestDto.getCode());
            throw new ResourceAlreadyExistsException(ElectiveGroupMessages.CODE_EXISTS);
        }

        electiveGroupMapper.updateFromDto(requestDto, group);

        if (requestDto.getOptionLectureIds() != null) {
            group.setOptions(resolveLectures(requestDto.getOptionLectureIds()));
        }

        var saved = electiveGroupDao.save(group);
        log.info("Elective group updated with ID: {}", saved.getId());

        return toDto(saved);
    }

    @Override
    @Transactional
    public ElectiveGroupDto addOption(UUID id, UUID lectureId) {
        log.info("Adding lecture {} to elective group {}", lectureId, id);

        var group = require(id);
        var lecture = requireLecture(lectureId);

        if (!group.getOptions().add(lecture)) {
            log.error("Lecture {} is already an option of elective group {}", lectureId, id);
            throw new ResourceAlreadyExistsException(ElectiveGroupMessages.OPTION_ALREADY_ADDED);
        }

        refreshDerivedDegreeLevels(group);
        var saved = electiveGroupDao.save(group);
        log.info("Lecture {} added to elective group {}", lectureId, id);

        return toDto(saved);
    }

    @Override
    @Transactional
    public ElectiveGroupDto removeOption(UUID id, UUID lectureId) {
        log.info("Removing lecture {} from elective group {}", lectureId, id);

        var group = require(id);
        var lecture = requireLecture(lectureId);

        if (!group.getOptions().remove(lecture)) {
            log.error("Lecture {} is not an option of elective group {}", lectureId, id);
            throw new ResourceNotFoundException(ElectiveGroupMessages.OPTION_NOT_FOUND);
        }

        var saved = electiveGroupDao.save(group);
        log.info("Lecture {} removed from elective group {}", lectureId, id);

        return toDto(saved);
    }

    @Override
    @Transactional
    public void deleteElectiveGroup(UUID id) {
        log.info("Deleting elective group with ID: {}", id);

        var group = require(id);
        group.getOptions().clear();
        electiveGroupDao.delete(group);

        log.info("Elective group soft deleted with ID: {}", id);
    }

    private ElectiveGroup require(UUID id) {
        return electiveGroupDao.findById(id).orElseThrow(() -> {
            log.error("Elective group with ID {} not found.", id);
            return new ResourceNotFoundException(ElectiveGroupMessages.ELECTIVE_GROUP_NOT_FOUND);
        });
    }

    private Lecture requireLecture(UUID lectureId) {
        return lectureDao.findById(lectureId).orElseThrow(() -> {
            log.error("Lecture with ID {} not found.", lectureId);
            return new ResourceNotFoundException(LectureMessages.LECTURE_NOT_FOUND);
        });
    }

    private Set<Lecture> resolveLectures(Set<UUID> lectureIds) {
        if (lectureIds == null || lectureIds.isEmpty()) {
            return new LinkedHashSet<>();
        }

        var lectures = new LinkedHashSet<Lecture>();
        for (var lectureId : lectureIds) {
            lectures.add(requireLecture(lectureId));
        }
        return lectures;
    }

    private void refreshDerivedDegreeLevels(ElectiveGroup group) {
        if (group.getDegreeLevels() == null || group.getDegreeLevels().isEmpty()) {
            group.setDegreeLevels(deriveDegreeLevels(group.getOptions()));
        }
    }

    private Set<DegreeLevel> deriveDegreeLevels(Set<Lecture> options) {
        var levels = new LinkedHashSet<DegreeLevel>();
        for (var lecture : options) {
            if (lecture.getDegreeLevels() != null) {
                levels.addAll(lecture.getDegreeLevels());
            }
        }
        return levels;
    }

    private ElectiveGroupDto toDto(ElectiveGroup group) {
        var dto = electiveGroupMapper.toDto(group);
        dto.setOptions(sortedOptions(group));
        dto.setOptionCount(dto.getOptions().size());
        return dto;
    }

    private List<ElectiveGroupOptionDto> sortedOptions(ElectiveGroup group) {
        return group.getOptions().stream()
                .sorted(Comparator.comparing(Lecture::getCode, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(electiveGroupMapper::toOptionDto)
                .toList();
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
