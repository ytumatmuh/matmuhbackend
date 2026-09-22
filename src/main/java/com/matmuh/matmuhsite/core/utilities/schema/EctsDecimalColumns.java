package com.matmuh.matmuhsite.core.utilities.schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@Order(6)
@Transactional
public class EctsDecimalColumns implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(EctsDecimalColumns.class);

    private static final List<String> TABLES = List.of("lectures", "elective_groups");

    private static final String COLUMN_TYPE = """
            SELECT data_type FROM information_schema.columns
            WHERE table_schema = current_schema() AND table_name = ? AND column_name = 'ects'
            """;

    private final JdbcTemplate jdbcTemplate;

    public EctsDecimalColumns(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Lisansüstü AKTS 7,5 gibi kesirli; kolon integer kaldığı sürece Postgres 7,5'i 8'e yuvarlar.
    // ddl-auto kolon tipini değiştirmediği için dönüşüm açılışta yapılır.
    @Override
    public void run(ApplicationArguments args) {
        var changed = widen();
        if (!changed.isEmpty()) {
            logger.info("ECTS columns widened to numeric(4,1): {}", changed);
        }
    }

    public List<String> widen() {
        var changed = new ArrayList<String>();
        for (var table : TABLES) {
            var type = jdbcTemplate.query(COLUMN_TYPE, (rs, i) -> rs.getString(1), table).stream().findFirst().orElse(null);
            if (type != null && List.of("smallint", "integer", "bigint").contains(type)) {
                jdbcTemplate.execute("ALTER TABLE \"" + table + "\" ALTER COLUMN \"ects\" TYPE numeric(4,1)");
                changed.add(table + ".ects");
            }
        }
        return changed;
    }
}
