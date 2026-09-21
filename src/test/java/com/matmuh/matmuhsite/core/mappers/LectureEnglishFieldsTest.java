package com.matmuh.matmuhsite.core.mappers;

import com.matmuh.matmuhsite.core.dtos.electiveGroup.request.UpdateElectiveGroupRequestDto;
import com.matmuh.matmuhsite.core.dtos.lecture.request.CreateLectureRequestDto;
import com.matmuh.matmuhsite.core.dtos.lecture.request.UpdateLectureRequestDto;
import com.matmuh.matmuhsite.core.dtos.lecture.response.SyllabusWeekDto;
import com.matmuh.matmuhsite.entities.ElectiveGroup;
import com.matmuh.matmuhsite.entities.Lecture;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LectureEnglishFieldsTest {

    private final LectureMapper lectureMapper = Mappers.getMapper(LectureMapper.class);
    private final ElectiveGroupMapper electiveGroupMapper = Mappers.getMapper(ElectiveGroupMapper.class);

    // Ziyaretçinin okuduğu her serbest metnin İngilizcesi var: ad, içerik, değerlendirme,
    // kaynaklar ve haftalık programın her satırı (ayrı dizi değil, hafta kaymasın).
    @Test
    void englishNameAndAboutRoundTripThroughRestAndCms() {
        var request = new CreateLectureRequestDto();
        request.setCode("MTM1501");
        request.setName("Analiz I");
        request.setNameEn("Analysis I");
        request.setAbout("Limit, süreklilik, türev.");
        request.setAboutEn("Limits, continuity, derivatives.");
        request.setGradingPolicy("Vize %40, final %60");
        request.setGradingPolicyEn("Midterm 40%, final 60%");
        request.setResources("Thomas, Kalkülüs");
        request.setResourcesEn("Thomas, Calculus");
        request.setSyllabus(List.of(new SyllabusWeekDto(1, "Limit", "Limits")));

        var dto = lectureMapper.toDto(lectureMapper.toEntity(request));

        assertEquals("Analysis I", dto.getNameEn());
        assertEquals("Limits, continuity, derivatives.", dto.getAboutEn());
        assertEquals("Midterm 40%, final 60%", dto.getGradingPolicyEn());
        assertEquals("Thomas, Calculus", dto.getResourcesEn());
        assertEquals("Limits", dto.getSyllabus().get(0).getTopicEn());
        assertEquals("Limit", dto.getSyllabus().get(0).getTopic());
        assertEquals("Analiz I", dto.getName());
        assertEquals("Analysis I", electiveGroupMapper.toOptionDto(lectureMapper.toEntity(request)).getNameEn());
    }

    // PATCH kısmi: İngilizce alan gönderilmezse eldeki korunur, Türkçe alan da dokunulmaz.
    @Test
    void partialUpdateKeepsTheOtherLanguage() {
        var lecture = Lecture.builder().code("MTM1501").name("Analiz I").nameEn("Analysis I").build();
        var update = new UpdateLectureRequestDto();
        update.setAboutEn("Only the English about changes.");

        lectureMapper.updateLectureFromDto(update, lecture);

        assertEquals("Analysis I", lecture.getNameEn());
        assertEquals("Analiz I", lecture.getName());
        assertEquals("Only the English about changes.", lecture.getAboutEn());
        assertNull(lecture.getAbout());
    }

    @Test
    void electiveGroupCarriesAnEnglishName() {
        var group = new ElectiveGroup();
        group.setCode("MES2-3G");
        group.setName("Mesleki Seçmeli 2");
        var update = new UpdateElectiveGroupRequestDto();
        update.setNameEn("Professional Elective 2");
        update.setAboutEn("Pick one from the pool.");

        electiveGroupMapper.updateFromDto(update, group);

        assertEquals("Professional Elective 2", electiveGroupMapper.toDto(group).getNameEn());
        assertEquals("Pick one from the pool.", electiveGroupMapper.toDto(group).getAboutEn());
        assertEquals("Mesleki Seçmeli 2", group.getName());
    }
}
