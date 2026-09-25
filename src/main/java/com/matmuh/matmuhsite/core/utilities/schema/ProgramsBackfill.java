package com.matmuh.matmuhsite.core.utilities.schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Order(7)
@Transactional
public class ProgramsBackfill implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(ProgramsBackfill.class);

    private static final String PROGRAM_OF_LEVEL = """
            CASE degree_level
                WHEN 'UNDERGRADUATE' THEN 'UNDERGRADUATE'
                WHEN 'MASTERS' THEN 'MASTERS_THESIS'
                WHEN 'DOCTORATE' THEN 'DOCTORATE'
            END""";

    private final JdbcTemplate jdbcTemplate;

    public ProgramsBackfill(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Program alanı 25 Eylül'de geldi; mevcut dersler ve seçmeli gruplar boş kalmasın diye öğrenim
    // düzeyinden bir kez doldurulur. Yalnız hedef tablo tamamen boşken çalışır: sonradan programı
    // bilerek boşaltılan kayda bir sonraki açılışta dokunmaz.
    @Override
    public void run(ApplicationArguments args) {
        var filled = backfill();
        if (!filled.isEmpty()) {
            logger.info("Programs backfilled from degree levels: {}", filled);
        }
    }

    public Map<String, Integer> backfill() {
        var filled = new LinkedHashMap<String, Integer>();
        copy("lecture_degree_levels", "lecture_programs", "lecture_id", filled);
        copy("elective_group_degree_levels", "elective_group_programs", "elective_group_id", filled);
        return filled;
    }

    private void copy(String source, String target, String ownerColumn, Map<String, Integer> filled) {
        if (!exists(source) || !exists(target)) {
            return;
        }
        var present = jdbcTemplate.queryForObject("SELECT count(*) FROM " + target, Integer.class);
        if (present != null && present > 0) {
            return;
        }
        var rows = jdbcTemplate.update("INSERT INTO " + target + " (" + ownerColumn + ", program) "
                + "SELECT DISTINCT " + ownerColumn + ", " + PROGRAM_OF_LEVEL + " FROM " + source);
        if (rows > 0) {
            filled.put(target, rows);
        }
    }

    private boolean exists(String table) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject("SELECT to_regclass(?) IS NOT NULL", Boolean.class, table));
    }
}
