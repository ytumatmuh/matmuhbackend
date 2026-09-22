package com.matmuh.matmuhsite.core.utilities.schema;

import com.matmuh.matmuhsite.core.utilities.schema.TextColumnWidener.Widened;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class TextColumnWidenerDbTest {

    private static final String TABLE = "lecture_syllabus";
    private static final String COLUMN = "topic_en";

    private static final String DATA_TYPE = """
            SELECT data_type FROM information_schema.columns
            WHERE table_schema = current_schema() AND table_name = ? AND column_name = ?
            """;

    @Autowired
    private TextColumnWidener widener;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void widensANarrowedColumnAndThenReportsNothingToDo() {
        narrowToVarchar();
        assertEquals("character varying", dataType());

        var widened = widener.widen();

        assertEquals(List.of(TABLE + "." + COLUMN), widened.stream().map(Widened::qualifiedName).toList());
        assertEquals("character varying", widened.get(0).previousType());
        assertEquals("text", dataType());

        assertTrue(widener.widen().isEmpty());
        assertEquals("text", dataType());
    }

    // USING left(...): test veritabanında başka bir testten kalmış uzun bir konu başlığı
    // daraltmayı patlatmasın; prod'daki bayat kolon zaten varchar(255).
    private void narrowToVarchar() {
        jdbc.execute("ALTER TABLE " + TABLE + " ALTER COLUMN " + COLUMN
                + " TYPE varchar(255) USING left(" + COLUMN + ", 255)");
    }

    private String dataType() {
        return jdbc.queryForObject(DATA_TYPE, String.class, TABLE, COLUMN);
    }
}
