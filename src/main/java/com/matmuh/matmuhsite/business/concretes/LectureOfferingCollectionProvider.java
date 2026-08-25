package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.CalendarAdminService;
import com.matmuh.matmuhsite.business.abstracts.CmsCollectionProvider;
import com.matmuh.matmuhsite.business.abstracts.LectureOfferingService;
import com.matmuh.matmuhsite.business.constants.CmsMessages;
import com.matmuh.matmuhsite.business.constants.LectureOfferingCollectionSchema;
import com.matmuh.matmuhsite.core.dtos.calendar.request.SaveScheduleSlotRequestDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionItemDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionListDto;
import com.matmuh.matmuhsite.core.dtos.lectureOfferings.request.CreateLectureOfferingRequestDto;
import com.matmuh.matmuhsite.core.dtos.lectureOfferings.request.UpdateLectureOfferingRequestDto;
import com.matmuh.matmuhsite.core.dtos.lectureOfferings.response.ExamWeightDto;
import com.matmuh.matmuhsite.core.exceptions.CmsValidationException;
import com.matmuh.matmuhsite.core.exceptions.MatmuhException;
import com.matmuh.matmuhsite.core.exceptions.ResourceNotFoundException;
import com.matmuh.matmuhsite.core.helpers.InstructorNames;
import com.matmuh.matmuhsite.core.helpers.MessageResolver;
import com.matmuh.matmuhsite.dataAccess.abstracts.LectureDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.LectureOfferingDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.StaffDao;
import com.matmuh.matmuhsite.entities.InstructionLanguage;
import com.matmuh.matmuhsite.entities.LectureOffering;
import com.matmuh.matmuhsite.entities.ScheduleSlot;
import com.matmuh.matmuhsite.entities.Semester;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class LectureOfferingCollectionProvider implements CmsCollectionProvider {

    private final LectureOfferingService lectureOfferingService;
    private final CalendarAdminService calendarAdminService;
    private final LectureOfferingDao lectureOfferingDao;
    private final LectureDao lectureDao;
    private final StaffDao staffDao;
    private final JsonMapper objectMapper;
    private final MessageResolver messageResolver;

    public LectureOfferingCollectionProvider(LectureOfferingService lectureOfferingService,
                                             CalendarAdminService calendarAdminService,
                                             LectureOfferingDao lectureOfferingDao,
                                             LectureDao lectureDao,
                                             StaffDao staffDao,
                                             JsonMapper objectMapper,
                                             MessageResolver messageResolver) {
        this.lectureOfferingService = lectureOfferingService;
        this.calendarAdminService = calendarAdminService;
        this.lectureOfferingDao = lectureOfferingDao;
        this.lectureDao = lectureDao;
        this.staffDao = staffDao;
        this.objectMapper = objectMapper;
        this.messageResolver = messageResolver;
    }


    public static String slugOf(String lectureCode, String academicYear, Semester semester, int groupNumber) {
        return (lectureCode + "-" + academicYear + "-" + semester.name() + "-" + groupNumber).toLowerCase(Locale.ROOT);
    }

    @Override
    public String collectionKey() {
        return LectureOfferingCollectionSchema.KEY;
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionListDto list(ObjectNode filters, String locale, int offset, int limit) {
        var sort = Sort.by(Sort.Direction.DESC, "academicYear").and(Sort.by(Sort.Direction.ASC, "groupNumber"));
        var page = lectureOfferingDao.findAll(PageRequest.of(offset / Math.max(limit, 1), Math.max(limit, 1), sort));

        var items = page.getContent().stream().map(this::toItem).toList();
        return new CollectionListDto(items, page.getTotalElements(), offset, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionItemDto getBySlug(String slug, String locale) {
        return toItem(requireBySlug(slug));
    }

    @Override
    @Transactional
    public CollectionItemDto create(ObjectNode data, String locale) {
        var lectureCode = requireText(data, LectureOfferingCollectionSchema.FIELD_LECTURE_CODE);
        var lecture = lectureDao.findByCodeIgnoreCase(lectureCode)
                .orElseThrow(() -> new CmsValidationException("Ders bulunamadı: " + lectureCode));

        var request = new CreateLectureOfferingRequestDto();
        request.setAcademicYear(requireText(data, LectureOfferingCollectionSchema.FIELD_ACADEMIC_YEAR));
        request.setSemester(semester(data));
        request.setGroupNumber(intValue(data, LectureOfferingCollectionSchema.FIELD_GROUP_NUMBER));
        request.setStaffId(staffId(data));
        request.setInstructorRawName(text(data, LectureOfferingCollectionSchema.FIELD_INSTRUCTOR_RAW_NAME));
        request.setLanguage(language(data));
        request.setExamWeights(examWeights(data));

        var created = lectureOfferingService.createOffering(lecture.getId(), request);
        var offering = lectureOfferingDao.findById(created.getId())
                .orElseThrow(() -> new ResourceNotFoundException(CmsMessages.COLLECTION_ITEM_NOT_FOUND));

        syncSlots(offering, data);
        return toItem(offering);
    }

    @Override
    @Transactional
    public CollectionItemDto upsert(String slug, ObjectNode data, Integer version, String locale) {
        var offering = requireBySlug(slug);

        var request = new UpdateLectureOfferingRequestDto();
        request.setAcademicYear(text(data, LectureOfferingCollectionSchema.FIELD_ACADEMIC_YEAR));
        request.setSemester(data.has(LectureOfferingCollectionSchema.FIELD_SEMESTER) ? semester(data) : null);
        request.setGroupNumber(data.has(LectureOfferingCollectionSchema.FIELD_GROUP_NUMBER)
                ? intValue(data, LectureOfferingCollectionSchema.FIELD_GROUP_NUMBER) : null);
        request.setStaffId(staffId(data));
        request.setInstructorRawName(text(data, LectureOfferingCollectionSchema.FIELD_INSTRUCTOR_RAW_NAME));
        request.setLanguage(language(data));
        request.setExamWeights(examWeights(data));

        lectureOfferingService.updateOffering(offering.getId(), request);

        var refreshed = lectureOfferingDao.findById(offering.getId())
                .orElseThrow(() -> new ResourceNotFoundException(CmsMessages.COLLECTION_ITEM_NOT_FOUND));

        syncSlots(refreshed, data);
        return toItem(refreshed);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySlug(String slug) {
        return findBySlug(slug).isPresent();
    }


    private void syncSlots(LectureOffering offering, ObjectNode data) {
        var node = data.get(LectureOfferingCollectionSchema.FIELD_SCHEDULE_SLOTS);
        if (node == null || node.isNull()) {
            return;
        }
        if (!node.isArray()) {
            throw new CmsValidationException("scheduleSlots bir dizi olmalı.");
        }

        var existing = new LinkedHashMap<String, ScheduleSlot>();
        for (var slot : calendarAdminService.listSlots(offering.getId())) {
            existing.put(slotKey(slot.getDayOfWeek(), slot.getStartTime()), slot);
        }

        var seen = new ArrayList<String>();

        for (var index = 0; index < node.size(); index++) {
            var row = node.get(index);
            var request = slotRequest(offering.getId(), row, index);
            var key = slotKey(request.getDayOfWeek(), request.getStartTime());
            var current = existing.get(key);

            try {
                calendarAdminService.saveSlot(current == null ? null : current.getId(), request);
            } catch (MatmuhException exception) {
                // Gömülü kaydetmede tek istek gidiyor; editör hangi satırın çakıştığını bilmeli.
                throw new CmsValidationException(LectureOfferingCollectionSchema.FIELD_SCHEDULE_SLOTS
                        + "[" + index + "]: "
                        + messageResolver.resolve(exception.getMessage(), exception.getMessageArguments()));
            }

            seen.add(key);
        }

        existing.forEach((key, slot) -> {
            if (!seen.contains(key)) {
                calendarAdminService.deleteSlot(slot.getId());
            }
        });
    }

    private String slotKey(DayOfWeek dayOfWeek, LocalTime startTime) {
        return dayOfWeek + "@" + startTime;
    }

    private SaveScheduleSlotRequestDto slotRequest(UUID offeringId, tools.jackson.databind.JsonNode row, int index) {
        var request = new SaveScheduleSlotRequestDto();
        request.setLectureOfferingId(offeringId);
        request.setDayOfWeek(enumValue(DayOfWeek.class, textOf(row, "dayOfWeek"), "scheduleSlots[" + index + "].dayOfWeek"));
        request.setStartTime(time(textOf(row, "startTime"), "scheduleSlots[" + index + "].startTime"));
        request.setEndTime(time(textOf(row, "endTime"), "scheduleSlots[" + index + "].endTime"));
        request.setClassroom(textOf(row, "classroom"));
        request.setOnline(row.has("online") && row.get("online").asBoolean(false));
        return request;
    }

    private LocalTime time(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new CmsValidationException(field + " zorunlu (HH:mm).");
        }
        try {
            return LocalTime.parse(value.trim());
        } catch (RuntimeException exception) {
            throw new CmsValidationException(field + " HH:mm biçiminde olmalı: " + value);
        }
    }

    private <E extends Enum<E>> E enumValue(Class<E> type, String value, String field) {
        if (value == null || value.isBlank()) {
            throw new CmsValidationException(field + " zorunlu.");
        }
        try {
            return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new CmsValidationException(field + " geçersiz: " + value);
        }
    }

    private LectureOffering requireBySlug(String slug) {
        return findBySlug(slug).orElseThrow(() -> new ResourceNotFoundException(
                CmsMessages.COLLECTION_ITEM_NOT_FOUND + LectureOfferingCollectionSchema.KEY + "/" + slug));
    }


    private Optional<LectureOffering> findBySlug(String slug) {
        if (slug == null) {
            return Optional.empty();
        }

        var parts = slug.split("-");
        if (parts.length < 5) {
            return Optional.empty();
        }

        var groupNumber = parts[parts.length - 1];
        var semester = parts[parts.length - 2];
        var academicYear = parts[parts.length - 4] + "-" + parts[parts.length - 3];
        var lectureCode = String.join("-", List.of(parts).subList(0, parts.length - 4));

        try {
            return lectureDao.findByCodeIgnoreCase(lectureCode).flatMap(lecture ->
                    lectureOfferingDao.findByLectureIdAndAcademicYearAndSemesterAndGroupNumber(
                            lecture.getId(), academicYear,
                            Semester.valueOf(semester.toUpperCase(Locale.ROOT)),
                            Integer.parseInt(groupNumber)));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private UUID staffId(ObjectNode data) {
        var slug = text(data, LectureOfferingCollectionSchema.FIELD_STAFF_SLUG);
        if (slug == null) {
            return null;
        }
        return staffDao.findBySlug(slug)
                .orElseThrow(() -> new CmsValidationException("Personel bulunamadı: " + slug))
                .getId();
    }

    private List<ExamWeightDto> examWeights(ObjectNode data) {
        var node = data.get(LectureOfferingCollectionSchema.FIELD_EXAM_WEIGHTS);
        if (node == null || node.isNull() || !node.isArray()) {
            return null;
        }
        return objectMapper.convertValue(node, objectMapper.getTypeFactory()
                .constructCollectionType(List.class, ExamWeightDto.class));
    }

    private Semester semester(ObjectNode data) {
        return enumValue(Semester.class, requireText(data, LectureOfferingCollectionSchema.FIELD_SEMESTER),
                LectureOfferingCollectionSchema.FIELD_SEMESTER);
    }

    private InstructionLanguage language(ObjectNode data) {
        var value = text(data, LectureOfferingCollectionSchema.FIELD_LANGUAGE);
        return value == null ? null
                : enumValue(InstructionLanguage.class, value, LectureOfferingCollectionSchema.FIELD_LANGUAGE);
    }

    private int intValue(ObjectNode data, String field) {
        var node = data.get(field);
        if (node == null || node.isNull() || !node.isNumber()) {
            throw new CmsValidationException(field + " zorunlu ve sayı olmalı.");
        }
        return node.asInt();
    }

    private String requireText(ObjectNode data, String field) {
        var value = text(data, field);
        if (value == null) {
            throw new CmsValidationException(field + " zorunlu.");
        }
        return value;
    }

    private String text(ObjectNode data, String field) {
        return textOf(data, field);
    }

    private String textOf(tools.jackson.databind.JsonNode node, String field) {
        var value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        var text = value.asString().trim();
        return text.isEmpty() ? null : text;
    }

    private CollectionItemDto toItem(LectureOffering offering) {
        var lecture = offering.getLecture();
        var data = objectMapper.createObjectNode();

        data.put(LectureOfferingCollectionSchema.FIELD_LECTURE_CODE, lecture == null ? null : lecture.getCode());
        data.put("lectureName", lecture == null ? null : lecture.getName());
        data.put(LectureOfferingCollectionSchema.FIELD_ACADEMIC_YEAR, offering.getAcademicYear());
        data.put(LectureOfferingCollectionSchema.FIELD_SEMESTER, offering.getSemester().name());
        data.put(LectureOfferingCollectionSchema.FIELD_GROUP_NUMBER, offering.getGroupNumber());
        data.put(LectureOfferingCollectionSchema.FIELD_STAFF_SLUG,
                offering.getStaff() == null ? null : offering.getStaff().getSlug());
        data.put(LectureOfferingCollectionSchema.FIELD_INSTRUCTOR_RAW_NAME, offering.getInstructorRawName());
        data.put("instructorName", InstructorNames.of(offering));
        data.put(LectureOfferingCollectionSchema.FIELD_LANGUAGE,
                offering.getLanguage() == null ? null : offering.getLanguage().name());

        var weights = data.putArray(LectureOfferingCollectionSchema.FIELD_EXAM_WEIGHTS);
        offering.getExamWeights().forEach(weight -> {
            var row = weights.addObject();
            row.put("examType", weight.getExamType().name());
            row.put("weightPercent", weight.getWeightPercent());
        });

        var slots = data.putArray(LectureOfferingCollectionSchema.FIELD_SCHEDULE_SLOTS);
        calendarAdminService.listSlots(offering.getId()).stream()
                .sorted((left, right) -> {
                    var byDay = left.getDayOfWeek().compareTo(right.getDayOfWeek());
                    return byDay != 0 ? byDay : left.getStartTime().compareTo(right.getStartTime());
                })
                .forEach(slot -> {
                    var row = slots.addObject();
                    row.put("dayOfWeek", slot.getDayOfWeek().name());
                    row.put("startTime", slot.getStartTime().toString());
                    row.put("endTime", slot.getEndTime().toString());
                    row.put("classroom", slot.getClassroom());
                    row.put("online", slot.isOnline());
                });

        var item = new CollectionItemDto();
        item.setId(offering.getId());
        item.setCollectionKey(LectureOfferingCollectionSchema.KEY);
        item.setSlug(slugOf(lecture == null ? "?" : lecture.getCode(), offering.getAcademicYear(),
                offering.getSemester(), offering.getGroupNumber()));
        item.setData(data);
        item.setVersion(0);
        return item;
    }
}
