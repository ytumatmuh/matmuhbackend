package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.CalendarAdminService;
import com.matmuh.matmuhsite.business.abstracts.CmsCollectionService;
import com.matmuh.matmuhsite.business.constants.AcademicTermCollectionSchema;
import com.matmuh.matmuhsite.core.dtos.calendar.request.SaveAcademicTermRequestDto;
import com.matmuh.matmuhsite.core.exceptions.CmsValidationException;
import com.matmuh.matmuhsite.core.exceptions.ResourceNotFoundException;
import com.matmuh.matmuhsite.dataAccess.abstracts.AcademicTermDao;
import com.matmuh.matmuhsite.entities.Semester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Editör panelde academic-terms kaydını silmeye basınca 400 alıyordu; bu test gerçek
// Postgres'te "CMS'ten silinen dönem kendi tablosundan gerçekten gitti mi?" sorusunu cevaplar.
@SpringBootTest
class CmsAcademicTermDeleteDbTest {

    private static final String KEY = AcademicTermCollectionSchema.KEY;
    private static final String YEAR = "2096-2097";
    private static final String SLUG = YEAR + "-fall";
    private static final String USER = "cms-term-delete-test";

    @Autowired
    private CmsCollectionService collectionService;

    @Autowired
    private CalendarAdminService calendarAdminService;

    @Autowired
    private AcademicTermCollectionProvider provider;

    @Autowired
    private AcademicTermDao academicTermDao;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    @AfterEach
    void cleanUp() {
        jdbc.update("DELETE FROM academic_terms WHERE academic_year = ?", YEAR);
    }

    @Test
    void deletingAProviderBackedTermRemovesItFromItsOwnTable() {
        var term = calendarAdminService.saveTerm(request());
        assertTrue(provider.existsBySlug(SLUG));

        // Sağlayıcı sürüm tutmuyor (AcademicTerm'de @Version yok), okumada 0 dönüyor.
        var result = collectionService.archive(KEY, SLUG, 0, USER);

        assertEquals(KEY, result.collectionKey());
        assertEquals(SLUG, result.slug());
        assertEquals(0, result.version());

        assertFalse(provider.existsBySlug(SLUG));
        assertTrue(academicTermDao.findById(term.getId()).isEmpty());
        assertThrows(ResourceNotFoundException.class, () -> collectionService.getBySlug(KEY, SLUG, USER, null));
    }

    @Test
    void deletingWithoutAVersionIsRefusedAndKeepsTheRow() {
        calendarAdminService.saveTerm(request());

        assertThrows(CmsValidationException.class, () -> collectionService.archive(KEY, SLUG, null, USER));

        assertTrue(provider.existsBySlug(SLUG));
    }

    @Test
    void deletingAnUnknownSlugIs404() {
        assertThrows(ResourceNotFoundException.class, () -> collectionService.archive(KEY, SLUG, 0, USER));
    }

    private SaveAcademicTermRequestDto request() {
        var request = new SaveAcademicTermRequestDto();
        request.setAcademicYear(YEAR);
        request.setSemester(Semester.FALL);
        request.setStartDate(LocalDate.of(2096, 9, 21));
        request.setEndDate(LocalDate.of(2097, 1, 16));
        return request;
    }
}
