package com.matmuh.matmuhsite.core.utilities.schema;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class LectureNoteReviewStatusBackfillTest {

    private static final String MARK = "review-status-backfill-test";

    @Autowired
    private LectureNoteReviewStatusBackfill backfill;

    @Autowired
    private JdbcTemplate jdbc;

    @AfterEach
    void cleanUp() {
        jdbc.update("DELETE FROM lecture_notes WHERE title LIKE ?", MARK + "%");
        jdbc.update("DELETE FROM lectures WHERE code = ?", MARK);
    }

    private UUID lecture() {
        var id = UUID.randomUUID();
        jdbc.update("INSERT INTO lectures (id, code, name, slug, created_at, is_deleted, version) VALUES (?, ?, ?, ?, now(), false, 0)",
                id, MARK, MARK, MARK);
        return id;
    }

    private void note(UUID lectureId, String title, Boolean approved, String status) {
        jdbc.update("INSERT INTO lecture_notes (id, lecture_id, title, is_approved, review_status, view_count, created_at, is_deleted) "
                        + "VALUES (?, ?, ?, ?, ?, 0, now(), false)",
                UUID.randomUUID(), lectureId, title, approved, status);
    }

    private String statusOf(String title) {
        return jdbc.queryForObject("SELECT review_status FROM lecture_notes WHERE title = ?", String.class, title);
    }

    // Prod'daki eski kolon: Hibernate taze şemada oluşturmaz, entity artık okumuyor.
    @Test
    void legacyBooleanBecomesTheEnum() {
        jdbc.execute("ALTER TABLE lecture_notes ADD COLUMN IF NOT EXISTS is_approved BOOLEAN");
        var lectureId = lecture();
        note(lectureId, MARK + "-approved", true, null);
        note(lectureId, MARK + "-pending", false, null);
        note(lectureId, MARK + "-already", null, "REJECTED");

        assertEquals(2, backfill.backfill());
        assertEquals("APPROVED", statusOf(MARK + "-approved"));
        assertEquals("PENDING", statusOf(MARK + "-pending"));
        assertEquals("REJECTED", statusOf(MARK + "-already"));

        assertEquals(0, backfill.backfill());
    }
}
