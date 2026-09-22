package com.matmuh.matmuhsite.core.utilities.schema;

import com.matmuh.matmuhsite.core.dtos.lecture.request.CreateLectureRequestDto;
import com.matmuh.matmuhsite.core.mappers.LectureMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Lisansüstü AKTS 7,5; integer kolon ve alan bunu sessizce 7'ye kesiyordu (Egehan, 23 Eylül).
@SpringBootTest
class EctsDecimalColumnsTest {

    @Autowired
    private EctsDecimalColumns ectsDecimalColumns;

    @Autowired
    private LectureMapper lectureMapper;

    @Autowired
    private JdbcTemplate jdbc;

    @AfterEach
    void restore() {
        ectsDecimalColumns.widen();
    }

    private String typeOf(String table) {
        return jdbc.queryForObject("""
                SELECT data_type || '(' || numeric_precision || ',' || numeric_scale || ')'
                FROM information_schema.columns WHERE table_name = ? AND column_name = 'ects'
                """, String.class, table);
    }

    @Test
    void anIntegerEctsColumnBecomesNumeric() {
        jdbc.execute("ALTER TABLE lectures ALTER COLUMN ects TYPE integer USING ects::integer");
        jdbc.execute("ALTER TABLE elective_groups ALTER COLUMN ects TYPE integer USING ects::integer");

        assertEquals(List.of("lectures.ects", "elective_groups.ects"), ectsDecimalColumns.widen());
        assertEquals("numeric(4,1)", typeOf("lectures"));
        assertEquals("numeric(4,1)", typeOf("elective_groups"));
        assertTrue(ectsDecimalColumns.widen().isEmpty());
    }

    @Test
    void aFractionalEctsSurvivesTheMapping() {
        var request = new CreateLectureRequestDto();
        request.setCode("MTM5101");
        request.setName("Lisansüstü");
        request.setEcts(new BigDecimal("7.5"));

        assertEquals(new BigDecimal("7.5"), lectureMapper.toDto(lectureMapper.toEntity(request)).getEcts());
    }
}
