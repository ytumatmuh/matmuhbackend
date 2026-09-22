package com.matmuh.matmuhsite.core.utilities.schema;

import com.matmuh.matmuhsite.core.helpers.EntityColumns;
import com.matmuh.matmuhsite.core.helpers.EntityColumns.MappedColumn;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Order(1)
public class EnumCheckConstraintSynchronizer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(EnumCheckConstraintSynchronizer.class);

    private static final Pattern QUOTED_LITERAL = Pattern.compile("'((?:[^']|'')*)'");

    private static final String FIND_CHECKS = """
            SELECT con.conname, pg_get_constraintdef(con.oid) AS definition
            FROM pg_constraint con
            JOIN pg_class rel ON rel.oid = con.conrelid
            JOIN pg_namespace ns ON ns.oid = rel.relnamespace
            WHERE con.contype = 'c'
              AND ns.nspname = current_schema()
              AND rel.relname = ?
              AND pg_get_constraintdef(con.oid) LIKE ? ESCAPE '\\'
            """;

    private static final String TABLE_EXISTS = """
            SELECT count(*) FROM information_schema.tables
            WHERE table_schema = current_schema() AND table_name = ?
            """;

    private static final String COLUMN_EXISTS = """
            SELECT count(*) FROM information_schema.columns
            WHERE table_schema = current_schema() AND table_name = ? AND column_name = ?
            """;

    private final DataSource dataSource;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transaction;

    public EnumCheckConstraintSynchronizer(DataSource dataSource, JdbcTemplate jdbc,
                                           PlatformTransactionManager transactionManager) {
        this.dataSource = dataSource;
        this.jdbc = jdbc;
        this.transaction = new TransactionTemplate(transactionManager);
    }

    public record EnumColumn(String table, String column, Set<String> values) {
        String qualifiedName() {
            return table + "." + column;
        }
    }

    public record Change(String table, String column, List<String> dropped, String added) {
        String qualifiedName() {
            return table + "." + column;
        }
    }

    // Hata bilerek yakalanmiyor: yanlis CHECK sessiz prod arizasi, basarisiz deploy ise gorunur.
    @Override
    public void run(ApplicationArguments args) {
        var changes = synchronize();
        if (changes.isEmpty()) {
            log.debug("Enum CHECK constraints already match the Java enums");
            return;
        }
        for (var change : changes) {
            log.info("Rewrote enum CHECK on {}: dropped {} and added {}",
                    change.qualifiedName(), change.dropped(), change.added());
        }
    }

    public List<Change> synchronize() {
        var product = databaseProductName();
        if (!"PostgreSQL".equalsIgnoreCase(product)) {
            log.info("Skipping enum CHECK synchronization: datasource is {} rather than PostgreSQL", product);
            return List.of();
        }
        return transaction.execute(status -> {
            var changes = new ArrayList<Change>();
            for (var enumColumn : discover()) {
                var change = synchronizeColumn(enumColumn);
                if (change != null) {
                    changes.add(change);
                }
            }
            return changes;
        });
    }

    private String databaseProductName() {
        try {
            return JdbcUtils.extractDatabaseMetaData(dataSource, DatabaseMetaData::getDatabaseProductName);
        } catch (MetaDataAccessException e) {
            throw new IllegalStateException("Cannot read database metadata", e);
        }
    }

    private Change synchronizeColumn(EnumColumn enumColumn) {
        var table = enumColumn.table();
        var column = enumColumn.column();
        if (queryCount(TABLE_EXISTS, table) == 0) {
            log.info("Skipping {}: table does not exist", enumColumn.qualifiedName());
            return null;
        }
        if (queryCount(COLUMN_EXISTS, table, column) == 0) {
            log.info("Skipping {}: column does not exist", enumColumn.qualifiedName());
            return null;
        }

        var existing = jdbc.query(FIND_CHECKS,
                (rs, i) -> Map.entry(rs.getString("conname"), rs.getString("definition")),
                table, columnPattern(column));

        if (existing.size() == 1 && literalsOf(existing.get(0).getValue()).equals(enumColumn.values())) {
            return null;
        }

        var dropped = new ArrayList<String>();
        for (var constraint : existing) {
            jdbc.execute("ALTER TABLE " + quoteIdentifier(table) + " DROP CONSTRAINT " + quoteIdentifier(constraint.getKey()));
            dropped.add(constraint.getKey());
        }

        var constraintName = table + "_" + column + "_check";
        var literals = enumColumn.values().stream()
                .map(EnumCheckConstraintSynchronizer::quoteLiteral)
                .collect(Collectors.joining(", "));
        jdbc.execute("ALTER TABLE " + quoteIdentifier(table)
                + " ADD CONSTRAINT " + quoteIdentifier(constraintName)
                + " CHECK (" + quoteIdentifier(column) + " = ANY (ARRAY[" + literals + "]::text[]))");
        return new Change(table, column, dropped, constraintName);
    }

    private int queryCount(String sql, Object... args) {
        var count = jdbc.queryForObject(sql, Integer.class, args);
        return count == null ? 0 : count;
    }

    static String columnPattern(String column) {
        var escaped = column.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%(" + escaped + ")::text%";
    }

    static Set<String> literalsOf(String constraintDefinition) {
        var literals = new TreeSet<String>();
        Matcher matcher = QUOTED_LITERAL.matcher(constraintDefinition);
        while (matcher.find()) {
            literals.add(matcher.group(1).replace("''", "'"));
        }
        return literals;
    }

    private static String quoteIdentifier(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }

    private static String quoteLiteral(String value) {
        return "'" + value.replace("'", "''") + "'";
    }

    static List<EnumColumn> discover() {
        var found = new LinkedHashSet<EnumColumn>();
        for (var mapped : EntityColumns.discover()) {
            if (isStringEnumerated(mapped)) {
                found.add(new EnumColumn(mapped.table(), mapped.column(), enumNames(mapped.type())));
            }
        }
        return found.stream()
                .sorted(Comparator.comparing(EnumColumn::table).thenComparing(EnumColumn::column))
                .toList();
    }

    private static boolean isStringEnumerated(MappedColumn mapped) {
        var enumerated = mapped.field().getAnnotation(Enumerated.class);
        return mapped.type().isEnum() && enumerated != null && enumerated.value() == EnumType.STRING;
    }

    private static Set<String> enumNames(Class<?> enumType) {
        return Arrays.stream(enumType.getEnumConstants())
                .map(constant -> ((Enum<?>) constant).name())
                .collect(Collectors.toCollection(TreeSet::new));
    }
}
