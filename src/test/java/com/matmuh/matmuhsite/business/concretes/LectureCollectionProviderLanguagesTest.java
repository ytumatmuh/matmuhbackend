package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.LectureService;
import com.matmuh.matmuhsite.business.constants.LectureMessages;
import com.matmuh.matmuhsite.core.dtos.lecture.request.CreateLectureRequestDto;
import com.matmuh.matmuhsite.core.dtos.lecture.request.UpdateLectureRequestDto;
import com.matmuh.matmuhsite.core.dtos.lecture.response.LectureDto;
import com.matmuh.matmuhsite.core.exceptions.CmsValidationException;
import com.matmuh.matmuhsite.core.mappers.LectureMapper;
import com.matmuh.matmuhsite.dataAccess.abstracts.LectureDao;
import com.matmuh.matmuhsite.entities.InstructionLanguage;
import com.matmuh.matmuhsite.entities.Lecture;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// CollectionFilterParser StringArray süzgecini tek öğeli dizi olarak verir ("ENGLISH,TURKISH" bir öğe);
// sağlayıcı virgülden ayırıp "herhangi biri" kümesine çevirir.
class LectureCollectionProviderLanguagesTest {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private LectureService lectureService;
    private LectureDao lectureDao;
    private LectureCollectionProvider provider;

    @BeforeEach
    void setUp() {
        lectureService = mock(LectureService.class);
        lectureDao = mock(LectureDao.class);
        provider = new LectureCollectionProvider(
                lectureService,
                lectureDao,
                Mappers.getMapper(LectureMapper.class),
                MAPPER,
                Validation.buildDefaultValidatorFactory().getValidator());
        when(lectureDao.search(any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(Page.empty());
    }

    @Test
    void listPassesASingleLanguageToTheDao() {
        provider.list(object("""
                {"languages": ["ENGLISH"]}
                """), null, 0, 20);

        assertEquals(Set.of(InstructionLanguage.ENGLISH), searchedLanguages());
    }

    @Test
    void listSplitsCommaSeparatedLanguagesIntoAnyOf() {
        provider.list(object("""
                {"languages": ["english, TURKISH"]}
                """), null, 0, 20);

        assertEquals(Set.of(InstructionLanguage.ENGLISH, InstructionLanguage.TURKISH), searchedLanguages());
    }

    @Test
    void listWithoutALanguageFilterPassesNull() {
        provider.list(object("""
                {"term": 1}
                """), null, 0, 20);

        assertNull(searchedLanguages());
    }

    @Test
    void listRejectsAnUnknownLanguage() {
        var ex = assertThrows(CmsValidationException.class, () -> provider.list(object("""
                {"languages": ["FRENCH"]}
                """), null, 0, 20));

        assertEquals(List.of(LectureMessages.LANGUAGE_INVALID), ex.getErrors());
    }

    @Test
    void createMapsLanguagesIntoTheRequest() {
        when(lectureService.createLecture(any())).thenAnswer(invocation -> {
            CreateLectureRequestDto request = invocation.getArgument(0);
            var dto = new LectureDto();
            dto.setId(UUID.randomUUID());
            dto.setSlug("mtm1501");
            dto.setLanguages(request.getLanguages());
            return dto;
        });
        when(lectureDao.findBySlug("mtm1501")).thenReturn(Optional.empty());

        var item = provider.create(object("""
                {"code": "MTM1501", "name": "Analiz I", "languages": ["TURKISH", "ENGLISH"]}
                """), null);

        var captor = ArgumentCaptor.forClass(CreateLectureRequestDto.class);
        verify(lectureService).createLecture(captor.capture());
        assertEquals(Set.of(InstructionLanguage.TURKISH, InstructionLanguage.ENGLISH), captor.getValue().getLanguages());
        assertEquals(2, item.getData().get("languages").size());
    }

    @Test
    void upsertWithoutLanguagesLeavesTheRequestFieldNull() {
        var lecture = Lecture.builder().id(UUID.randomUUID()).slug("mtm1501").build();
        when(lectureDao.findBySlug("mtm1501")).thenReturn(Optional.of(lecture));
        when(lectureService.updateLecture(any(), any())).thenReturn(new LectureDto());

        provider.upsert("mtm1501", object("""
                {"name": "Analiz I"}
                """), null, null);

        var captor = ArgumentCaptor.forClass(UpdateLectureRequestDto.class);
        verify(lectureService).updateLecture(any(), captor.capture());
        assertNull(captor.getValue().getLanguages());
    }

    @Test
    void getBySlugMapsLanguagesOut() {
        var lecture = Lecture.builder().id(UUID.randomUUID()).code("MTM1501").name("Analiz I").slug("mtm1501")
                .languages(new LinkedHashSet<>(List.of(InstructionLanguage.ENGLISH))).build();
        when(lectureDao.findBySlug("mtm1501")).thenReturn(Optional.of(lecture));

        var item = provider.getBySlug("mtm1501", null);

        var languages = item.getData().get("languages");
        assertEquals(1, languages.size());
        assertEquals("ENGLISH", languages.get(0).asString());
    }

    @SuppressWarnings("unchecked")
    private Collection<InstructionLanguage> searchedLanguages() {
        var captor = ArgumentCaptor.forClass(Collection.class);
        verify(lectureDao).search(any(), any(), any(), any(), any(), any(), captor.capture(), any(), any());
        return captor.getValue();
    }

    private static ObjectNode object(String json) {
        return (ObjectNode) MAPPER.readTree(json);
    }
}
