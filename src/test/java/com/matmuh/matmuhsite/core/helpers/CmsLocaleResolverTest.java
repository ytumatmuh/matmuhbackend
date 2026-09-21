package com.matmuh.matmuhsite.core.helpers;

import com.matmuh.matmuhsite.core.exceptions.CmsValidationException;
import com.matmuh.matmuhsite.dataAccess.abstracts.cms.CmsLocaleDao;
import com.matmuh.matmuhsite.entities.cms.CmsLocale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CmsLocaleResolverTest {

    private FakeDao dao;
    private CmsLocaleResolver resolver;

    @BeforeEach
    void setUp() {
        dao = new FakeDao();
        resolver = new CmsLocaleResolver(dao);
    }

    @Test
    void behavesAsBeforeWhenNoLocaleDeclared() {
        assertFalse(resolver.isLocalized());
        assertNull(resolver.defaultLocale());
        assertNull(resolver.resolveForRead("tr"));
        assertNull(resolver.requireForWrite(null));
    }

    @Test
    void rejectsWriteWithLocaleWhenNoneDeclared() {
        assertThrows(CmsValidationException.class, () -> resolver.requireForWrite("tr"));
    }

    @Test
    void firstDeclaredLocaleIsTheDefault() {
        resolver.replaceDeclared(List.of("tr", "en"));

        assertTrue(resolver.isLocalized());
        assertEquals("tr", resolver.defaultLocale());
        assertEquals(List.of("tr", "en"), resolver.declared());
    }

    @Test
    void readFallsBackToDefaultOnUnknownLocale() {
        resolver.replaceDeclared(List.of("tr", "en"));

        assertEquals("en", resolver.resolveForRead("en"));
        assertEquals("tr", resolver.resolveForRead("de"));
        assertEquals("tr", resolver.resolveForRead(null));
        assertEquals("tr", resolver.resolveForRead("  "));
    }

    @Test
    void writeRequiresAnExplicitLocaleWhenLocalesAreDeclared() {
        resolver.replaceDeclared(List.of("tr", "en"));

        assertEquals("en", resolver.requireForWrite("EN"));
        assertThrows(CmsValidationException.class, () -> resolver.requireForWrite("de"));

        // Varsayılana düşmek yok: locale'siz yazma içeriği yanlış dile koyabilirdi.
        // Çeviri düzenlerken locale unutulursa İngilizce metin Türkçe kaydın üstüne yazılır.
        assertThrows(CmsValidationException.class, () -> resolver.requireForWrite(null));
        assertThrows(CmsValidationException.class, () -> resolver.requireForWrite("  "));
    }

    @Test
    void writeErrorNamesTheCollectionAndAvailableLocales() {
        resolver.replaceDeclared(List.of("tr", "en"));

        var error = assertThrows(CmsValidationException.class,
                () -> resolver.requireForWrite(null, "announcements"));

        assertTrue(error.getMessage().contains("tr, en"));
        assertTrue(error.getMessage().contains("announcements"));
    }

    @Test
    void normalizesWithRootLocaleNotTurkish() {
        var previous = java.util.Locale.getDefault();
        try {
            java.util.Locale.setDefault(new java.util.Locale("tr", "TR"));
            // Türkçe yerelde düz toLowerCase "ID" -> "ıd" yapar ve kod doğrulamaya takılırdı.
            assertEquals("id", CmsLocaleResolver.normalize("ID"));
        } finally {
            java.util.Locale.setDefault(previous);
        }
    }

    @Test
    void replaceDeclaredNormalizesAndValidates() {
        resolver.replaceDeclared(List.of(" TR ", "en", "tr"));
        assertEquals(List.of("tr", "en"), resolver.declared());

        assertThrows(CmsValidationException.class, () -> resolver.replaceDeclared(List.of("tr_TR")));
        assertThrows(CmsValidationException.class, () -> resolver.replaceDeclared(List.of("türkçe")));
    }

    @Test
    void nullKeepsListAndEmptyClearsIt() {
        resolver.replaceDeclared(List.of("tr", "en"));

        assertEquals(List.of("tr", "en"), resolver.replaceDeclared(null));

        resolver.replaceDeclared(List.of());
        assertFalse(resolver.isLocalized());
    }

    /** Sadece bu testin kullandığı üç metodu karşılayan bellek içi sahte depo. */
    private static class FakeDao implements CmsLocaleDao {

        private final List<CmsLocale> rows = new ArrayList<>();

        @Override
        public List<CmsLocale> findAllByOrderByPositionAsc() {
            return rows.stream().sorted(Comparator.comparingInt(CmsLocale::getPosition)).toList();
        }

        @Override
        public void deleteAllInBatch() {
            rows.clear();
        }

        @Override
        public <S extends CmsLocale> S save(S entity) {
            rows.add(entity);
            return entity;
        }

        // --- kullanılmayanlar ---
        @Override public List<CmsLocale> findAll() { return List.copyOf(rows); }
        @Override public List<CmsLocale> findAll(org.springframework.data.domain.Sort sort) { throw new UnsupportedOperationException(); }
        @Override public org.springframework.data.domain.Page<CmsLocale> findAll(org.springframework.data.domain.Pageable pageable) { throw new UnsupportedOperationException(); }
        @Override public List<CmsLocale> findAllById(Iterable<String> ids) { throw new UnsupportedOperationException(); }
        @Override public <S extends CmsLocale> List<S> saveAll(Iterable<S> entities) { throw new UnsupportedOperationException(); }
        @Override public java.util.Optional<CmsLocale> findById(String id) { throw new UnsupportedOperationException(); }
        @Override public boolean existsById(String id) { throw new UnsupportedOperationException(); }
        @Override public long count() { return rows.size(); }
        @Override public void deleteById(String id) { throw new UnsupportedOperationException(); }
        @Override public void delete(CmsLocale entity) { throw new UnsupportedOperationException(); }
        @Override public void deleteAllById(Iterable<? extends String> ids) { throw new UnsupportedOperationException(); }
        @Override public void deleteAll(Iterable<? extends CmsLocale> entities) { throw new UnsupportedOperationException(); }
        @Override public void deleteAll() { rows.clear(); }
        @Override public void flush() { }
        @Override public <S extends CmsLocale> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends CmsLocale> List<S> saveAllAndFlush(Iterable<S> entities) { throw new UnsupportedOperationException(); }
        @Override public void deleteAllInBatch(Iterable<CmsLocale> entities) { throw new UnsupportedOperationException(); }
        @Override public void deleteAllByIdInBatch(Iterable<String> ids) { throw new UnsupportedOperationException(); }
        @Override public CmsLocale getOne(String id) { throw new UnsupportedOperationException(); }
        @Override public CmsLocale getById(String id) { throw new UnsupportedOperationException(); }
        @Override public CmsLocale getReferenceById(String id) { throw new UnsupportedOperationException(); }
        @Override public <S extends CmsLocale> java.util.Optional<S> findOne(org.springframework.data.domain.Example<S> example) { throw new UnsupportedOperationException(); }
        @Override public <S extends CmsLocale> List<S> findAll(org.springframework.data.domain.Example<S> example) { throw new UnsupportedOperationException(); }
        @Override public <S extends CmsLocale> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { throw new UnsupportedOperationException(); }
        @Override public <S extends CmsLocale> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { throw new UnsupportedOperationException(); }
        @Override public <S extends CmsLocale> long count(org.springframework.data.domain.Example<S> example) { throw new UnsupportedOperationException(); }
        @Override public <S extends CmsLocale> boolean exists(org.springframework.data.domain.Example<S> example) { throw new UnsupportedOperationException(); }
        @Override public <S extends CmsLocale, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { throw new UnsupportedOperationException(); }
    }
}
