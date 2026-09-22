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
@Order(4)
@Transactional
public class OfferingOrphanCleanup implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(OfferingOrphanCleanup.class);

    private static final String TABLES_EXIST = """
            SELECT to_regclass('lecture_offerings') IS NOT NULL
               AND to_regclass('schedule_slots') IS NOT NULL
               AND to_regclass('enrollments') IS NOT NULL
               AND to_regclass('calendar_events') IS NOT NULL
               AND to_regclass('lecture_notes') IS NOT NULL
            """;

    private static final String DELETED_OFFERINGS = "(SELECT id FROM lecture_offerings WHERE is_deleted)";

    private static final String DELETE_ORPHANS = "DELETE FROM %s WHERE lecture_offering_id IN " + DELETED_OFFERINGS;

    private static final String UNBIND_NOTES =
            "UPDATE lecture_notes SET lecture_offering_id = NULL WHERE lecture_offering_id IN " + DELETED_OFFERINGS;

    public record Result(int slots, int enrollments, int events, int notes) {

        public int total() {
            return slots + enrollments + events + notes;
        }
    }

    private final JdbcTemplate jdbcTemplate;

    public OfferingOrphanCleanup(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        var result = cleanUp();
        if (result.total() > 0) {
            logger.info("Offering orphan cleanup: {} slot(s), {} enrollment(s), {} calendar event(s) of deleted offerings dropped, {} note(s) unbound",
                    result.slots(), result.enrollments(), result.events(), result.notes());
        } else {
            logger.debug("Offering orphan cleanup: nothing to do");
        }
    }

    // Açılış silme yolu (OfferingDependents) saat, kayıt ve sınav tarihini artık açılışla düşürüyor
    // ve notun açılış bağını çözüyor; bu runner daha önce silinmiş açılışların geride bıraktıklarını
    // aynı kuralla temizler. Saat ve kayıt zaten görünmezdi ama sınav tarihi genel takvimde hayalet
    // olarak duruyor, açılışa bağlı notun tek okuması 500 dönüyordu.
    public Result cleanUp() {
        if (!Boolean.TRUE.equals(jdbcTemplate.queryForObject(TABLES_EXIST, Boolean.class))) {
            return new Result(0, 0, 0, 0);
        }
        return new Result(
                jdbcTemplate.update(DELETE_ORPHANS.formatted("schedule_slots")),
                jdbcTemplate.update(DELETE_ORPHANS.formatted("enrollments")),
                jdbcTemplate.update(DELETE_ORPHANS.formatted("calendar_events")),
                jdbcTemplate.update(UNBIND_NOTES));
    }
}
