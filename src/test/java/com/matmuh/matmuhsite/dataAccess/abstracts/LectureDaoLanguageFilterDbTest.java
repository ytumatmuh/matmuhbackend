package com.matmuh.matmuhsite.dataAccess.abstracts;

import com.matmuh.matmuhsite.entities.InstructionLanguage;
import com.matmuh.matmuhsite.entities.Lecture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class LectureDaoLanguageFilterDbTest {

    private static final String MARK = "lang-filter-db-test";

    private static final String CHECKS_ON_LANGUAGE = """
            SELECT pg_get_constraintdef(con.oid)
            FROM pg_constraint con
            JOIN pg_class rel ON rel.oid = con.conrelid
            JOIN pg_namespace ns ON ns.oid = rel.relnamespace
            WHERE con.contype = 'c'
              AND ns.nspname = current_schema()
              AND rel.relname = 'lecture_languages'
              AND pg_get_constraintdef(con.oid) LIKE '%(language)::text%' ESCAPE '\\'
            """;

    @Autowired
    private LectureDao lectureDao;

    @Autowired
    private JdbcTemplate jdbc;

    @AfterEach
    void cleanUp() {
        jdbc.update("DELETE FROM lecture_languages WHERE lecture_id IN (SELECT id FROM lectures WHERE code LIKE ?)", MARK + "%");
        jdbc.update("DELETE FROM lectures WHERE code LIKE ?", MARK + "%");
    }

    @Test
    void languageFilterMatchesAnyOfTheGivenLanguages() {
        var english = save("-en", InstructionLanguage.ENGLISH);
        var turkish = save("-tr", InstructionLanguage.TURKISH);
        var both = save("-both", InstructionLanguage.TURKISH, InstructionLanguage.ENGLISH);
        var none = save("-none");

        assertEquals(Set.of(english, both), codes(search(List.of(InstructionLanguage.ENGLISH))));
        assertEquals(Set.of(turkish, both), codes(search(List.of(InstructionLanguage.TURKISH))));
        assertEquals(Set.of(english, turkish, both),
                codes(search(List.of(InstructionLanguage.ENGLISH, InstructionLanguage.TURKISH))));
        assertEquals(Set.of(english, turkish, both, none), codes(search(null)));
        assertEquals(2, search(List.of(InstructionLanguage.ENGLISH)).getTotalElements());
    }

    @Test
    @Transactional
    void languagesAreStoredOncePerLectureAndReadBack() {
        var code = save("-both", InstructionLanguage.TURKISH, InstructionLanguage.ENGLISH);

        var rows = jdbc.queryForList("SELECT language FROM lecture_languages WHERE lecture_id = "
                + "(SELECT id FROM lectures WHERE code = ?) ORDER BY language", String.class, code);
        assertEquals(List.of("ENGLISH", "TURKISH"), rows);

        var reloaded = lectureDao.findByCodeIgnoreCase(code).orElseThrow();
        assertEquals(Set.of(InstructionLanguage.TURKISH, InstructionLanguage.ENGLISH), reloaded.getLanguages());
    }

    // ddl-auto tabloyu, EnumCheckConstraintSynchronizer CHECK'i kurar; ikisi de açılışta.
    @Test
    void languageColumnCarriesACheckWithEveryEnumValue() {
        var definitions = jdbc.queryForList(CHECKS_ON_LANGUAGE, String.class);

        assertEquals(1, definitions.size(), definitions.toString());
        for (var language : InstructionLanguage.values()) {
            assertTrue(definitions.get(0).contains("'" + language.name() + "'"), definitions.get(0));
        }
    }

    // Varlık üzerinden kaydetmek denetim dinleyicisini (system user) ister; öteki DB testleri gibi ham SQL.
    private String save(String suffix, InstructionLanguage... languages) {
        var code = MARK + suffix;
        var id = UUID.randomUUID();
        jdbc.update("INSERT INTO lectures (id, code, name, slug, weekly_hours, local_credit, ects, created_at, is_deleted, version) "
                        + "VALUES (?, ?, ?, ?, 0, 0, 0, now(), false, 0)",
                id, code, code, code);
        for (var language : languages) {
            jdbc.update("INSERT INTO lecture_languages (lecture_id, language) VALUES (?, ?)", id, language.name());
        }
        return code;
    }

    private Page<Lecture> search(Collection<InstructionLanguage> languages) {
        return lectureDao.search(null, null, null, null, null, null, languages, MARK,
                PageRequest.of(0, 50, Sort.by("code")));
    }

    private static Set<String> codes(Page<Lecture> page) {
        return page.getContent().stream().map(Lecture::getCode).collect(Collectors.toSet());
    }
}
