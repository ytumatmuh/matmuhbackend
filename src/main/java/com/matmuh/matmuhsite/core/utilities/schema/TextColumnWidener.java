package com.matmuh.matmuhsite.core.utilities.schema;

import com.matmuh.matmuhsite.core.helpers.EntityColumns;
import com.matmuh.matmuhsite.core.helpers.EntityColumns.MappedColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.List;

@Component
@Order(5)
@Transactional
public class TextColumnWidener implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(TextColumnWidener.class);

    private static final String TEXT = "text";

    private static final String TABLE_EXISTS = "SELECT to_regclass(?::text) IS NOT NULL";

    private static final String COLUMN_TYPE = """
            SELECT data_type FROM information_schema.columns
            WHERE table_schema = current_schema() AND table_name = ? AND column_name = ?
            """;

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public TextColumnWidener(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    public record Widened(String table, String column, String previousType) {

        public String qualifiedName() {
            return table + "." + column;
        }
    }

    // Hata bilerek yakalanmıyor: yarım göçmüş şema sessiz veri kaybı, başarısız deploy ise görünür.
    @Override
    public void run(ApplicationArguments args) {
        var widened = widen();
        if (widened.isEmpty()) {
            logger.debug("Every column mapped as TEXT is already text");
            return;
        }
        for (var change : widened) {
            logger.info("Widened {} from {} to text", change.qualifiedName(), change.previousType());
        }
    }

    // ddl-auto=update mevcut kolonun tipini asla genişletmez: @Column(columnDefinition = "TEXT")
    // eklenmeden önce oluşmuş kolonlar prod'da varchar(255) kaldı ve uzun içerik kaydedilirken
    // "value too long for type character varying(255)" veriyordu.
    public List<Widened> widen() {
        var product = databaseProductName();
        if (!"PostgreSQL".equalsIgnoreCase(product)) {
            logger.info("Skipping text column widening: datasource is {} rather than PostgreSQL", product);
            return List.of();
        }
        var widened = new ArrayList<Widened>();
        for (var column : discover()) {
            var change = widenColumn(column);
            if (change != null) {
                widened.add(change);
            }
        }
        return widened;
    }

    private Widened widenColumn(MappedColumn column) {
        var table = column.table();
        var name = column.column();
        if (!tableExists(table)) {
            logger.debug("Skipping {}: table does not exist", column.qualifiedName());
            return null;
        }
        var currentType = currentTypeOf(table, name);
        if (currentType == null) {
            logger.debug("Skipping {}: column does not exist", column.qualifiedName());
            return null;
        }
        if (TEXT.equalsIgnoreCase(currentType)) {
            return null;
        }
        jdbcTemplate.execute("ALTER TABLE " + quoteIdentifier(table)
                + " ALTER COLUMN " + quoteIdentifier(name) + " TYPE text");
        return new Widened(table, name, currentType);
    }

    private String databaseProductName() {
        try {
            return JdbcUtils.extractDatabaseMetaData(dataSource, DatabaseMetaData::getDatabaseProductName);
        } catch (MetaDataAccessException e) {
            throw new IllegalStateException("Cannot read database metadata", e);
        }
    }

    private boolean tableExists(String table) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(TABLE_EXISTS, Boolean.class, quoteIdentifier(table)));
    }

    private String currentTypeOf(String table, String column) {
        var types = jdbcTemplate.queryForList(COLUMN_TYPE, String.class, table, column);
        return types.isEmpty() ? null : types.get(0);
    }

    static List<MappedColumn> discover() {
        return EntityColumns.discover().stream()
                .filter(TextColumnWidener::isTextColumn)
                .toList();
    }

    private static boolean isTextColumn(MappedColumn column) {
        return column.type() == String.class && TEXT.equalsIgnoreCase(column.columnDefinition());
    }

    private static String quoteIdentifier(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }
}
