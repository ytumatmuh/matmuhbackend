package com.matmuh.matmuhsite.dataAccess.abstracts;

import com.matmuh.matmuhsite.entities.InstructionLanguage;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.util.EnumSet;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

// Hibernate boş IN listesi bağlayamaz; süzgeç yokken bayrak kapanır ve liste tüm dillerle dolar,
// böylece dili hiç girilmemiş dersler de listede kalır.
class LectureDaoLanguageFilterTest {

    private final LectureDao dao = mock(LectureDao.class, CALLS_REAL_METHODS);

    @Test
    void requestedLanguagesTurnTheFilterOn() {
        dao.search(null, null, null, null, null, List.of(InstructionLanguage.ENGLISH), null, Pageable.unpaged());

        verify(dao).searchMatching(isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(true), eq(EnumSet.of(InstructionLanguage.ENGLISH)), isNull(), any());
    }

    @Test
    void severalLanguagesAreBoundAsOneAnyOfList() {
        dao.search(null, null, null, null, null,
                List.of(InstructionLanguage.ENGLISH, InstructionLanguage.TURKISH), null, Pageable.unpaged());

        verify(dao).searchMatching(isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(true), eq(EnumSet.allOf(InstructionLanguage.class)), isNull(), any());
    }

    @Test
    void noLanguagesTurnTheFilterOff() {
        dao.search(null, null, null, null, null, null, null, Pageable.unpaged());

        verify(dao).searchMatching(isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(false), eq(EnumSet.allOf(InstructionLanguage.class)), isNull(), any());
    }

    @Test
    void anEmptyListMeansNoFilter() {
        dao.search(null, null, null, null, null, List.of(), null, Pageable.unpaged());

        verify(dao).searchMatching(isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(false), eq(EnumSet.allOf(InstructionLanguage.class)), isNull(), any());
    }
}
