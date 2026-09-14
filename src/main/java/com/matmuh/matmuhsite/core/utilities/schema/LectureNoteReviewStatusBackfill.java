package com.matmuh.matmuhsite.core.utilities.schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(3)
@Transactional
public class LectureNoteReviewStatusBackfill implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(LectureNoteReviewStatusBackfill.class);

    private static final String HAS_LEGACY_COLUMNS = """
            SELECT count(*) = 2 FROM information_schema.columns
            WHERE table_schema = current_schema() AND table_name = 'lecture_notes'
              AND column_name IN ('is_approved', 'review_status')
            """;

    // Eski boolean'dan enum'a: is_approved=true onaylı, false + onaylayan var reddedilmiş, gerisi bekliyor.
    private static final String BACKFILL = """
            UPDATE lecture_notes
            SET review_status = CASE
                    WHEN is_approved IS TRUE THEN 'APPROVED'
                    WHEN is_approved IS FALSE AND approved_by_id IS NOT NULL THEN 'REJECTED'
                    ELSE 'PENDING'
                END
            WHERE review_status IS NULL
            """;

    private final JdbcTemplate jdbcTemplate;

    public LectureNoteReviewStatusBackfill(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        var updated = backfill();
        if (updated > 0) {
            logger.info("Lecture note review status backfill: {} notes", updated);
        } else {
            logger.debug("Lecture note review status backfill: nothing to do");
        }
    }

    public int backfill() {
        if (!Boolean.TRUE.equals(jdbcTemplate.queryForObject(HAS_LEGACY_COLUMNS, Boolean.class))) {
            return 0;
        }
        return jdbcTemplate.update(BACKFILL);
    }
}
