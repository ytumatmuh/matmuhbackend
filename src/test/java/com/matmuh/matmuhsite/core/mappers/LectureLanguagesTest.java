package com.matmuh.matmuhsite.core.mappers;

import com.matmuh.matmuhsite.core.dtos.lecture.request.CreateLectureRequestDto;
import com.matmuh.matmuhsite.core.dtos.lecture.request.UpdateLectureRequestDto;
import com.matmuh.matmuhsite.entities.InstructionLanguage;
import com.matmuh.matmuhsite.entities.Lecture;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LectureLanguagesTest {

    private final LectureMapper lectureMapper = Mappers.getMapper(LectureMapper.class);
    private final ElectiveGroupMapper electiveGroupMapper = Mappers.getMapper(ElectiveGroupMapper.class);

    // Dil ders düzeyinde bir küme: iki dilde verilen ders ikisini de taşır.
    @Test
    void languagesRoundTripFromRequestToDto() {
        var request = new CreateLectureRequestDto();
        request.setCode("MTM1501");
        request.setName("Analiz I");
        request.setLanguages(new LinkedHashSet<>(List.of(InstructionLanguage.TURKISH, InstructionLanguage.ENGLISH)));

        var entity = lectureMapper.toEntity(request);
        var dto = lectureMapper.toDto(entity);

        assertEquals(Set.of(InstructionLanguage.TURKISH, InstructionLanguage.ENGLISH), entity.getLanguages());
        assertEquals(Set.of(InstructionLanguage.TURKISH, InstructionLanguage.ENGLISH), dto.getLanguages());
        assertEquals(Set.of(InstructionLanguage.TURKISH, InstructionLanguage.ENGLISH),
                electiveGroupMapper.toOptionDto(entity).getLanguages());
    }

    @Test
    void languagesDefaultToAnEmptySetWhenNotSent() {
        var request = new CreateLectureRequestDto();
        request.setCode("MTM1501");
        request.setName("Analiz I");

        var entity = lectureMapper.toEntity(request);

        assertTrue(entity.getLanguages().isEmpty());
        assertTrue(lectureMapper.toDto(entity).getLanguages().isEmpty());
    }

    // PATCH kısmi: alan gönderilmezse eldeki diller korunur.
    @Test
    void partialUpdateWithoutLanguagesKeepsThem() {
        var lecture = Lecture.builder().code("MTM1501").name("Analiz I")
                .languages(new LinkedHashSet<>(List.of(InstructionLanguage.ENGLISH))).build();
        var update = new UpdateLectureRequestDto();
        update.setName("Analiz I (güncel)");

        lectureMapper.updateLectureFromDto(update, lecture);

        assertEquals(Set.of(InstructionLanguage.ENGLISH), lecture.getLanguages());
        assertEquals("Analiz I (güncel)", lecture.getName());
    }

    // PATCH kısmi: alan gönderilirse küme olduğu gibi değiştirilir, birleştirilmez.
    @Test
    void partialUpdateWithLanguagesReplacesThem() {
        var lecture = Lecture.builder().code("MTM1501").name("Analiz I")
                .languages(new LinkedHashSet<>(List.of(InstructionLanguage.ENGLISH))).build();
        var update = new UpdateLectureRequestDto();
        update.setLanguages(Set.of(InstructionLanguage.TURKISH));

        lectureMapper.updateLectureFromDto(update, lecture);

        assertEquals(Set.of(InstructionLanguage.TURKISH), lecture.getLanguages());
    }

    @Test
    void partialUpdateWithAnEmptySetClearsThem() {
        var lecture = Lecture.builder().code("MTM1501").name("Analiz I")
                .languages(new LinkedHashSet<>(List.of(InstructionLanguage.ENGLISH))).build();
        var update = new UpdateLectureRequestDto();
        update.setLanguages(Set.of());

        lectureMapper.updateLectureFromDto(update, lecture);

        assertTrue(lecture.getLanguages().isEmpty());
    }
}
