package com.matmuh.matmuhsite.core.utilities.schema;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinTable;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.hibernate.boot.model.naming.PhysicalNamingStrategySnakeCaseImpl;
import org.hibernate.boot.model.naming.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
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

    static final String ENTITY_PACKAGE = "com.matmuh.matmuhsite.entities";

    private static final PhysicalNamingStrategySnakeCaseImpl NAMING = new PhysicalNamingStrategySnakeCaseImpl();
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
        for (var entity : entityClasses()) {
            collectEntityColumns(entity, found);
        }
        return found.stream()
                .sorted(Comparator.comparing(EnumColumn::table).thenComparing(EnumColumn::column))
                .toList();
    }

    private static List<Class<?>> entityClasses() {
        var scanner = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                return beanDefinition.getMetadata().isIndependent();
            }
        };
        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
        var classes = new ArrayList<Class<?>>();
        for (var definition : scanner.findCandidateComponents(ENTITY_PACKAGE)) {
            try {
                classes.add(Class.forName(definition.getBeanClassName()));
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Entity class vanished during scan: " + definition.getBeanClassName(), e);
            }
        }
        classes.sort(Comparator.comparing(Class::getName));
        return classes;
    }

    private static void collectEntityColumns(Class<?> entity, Set<EnumColumn> into) {
        var table = tableOf(entity);
        for (var field : persistentFields(entity)) {
            collectFieldColumns(field, table, Map.of(), into);
        }
    }

    private static void collectFieldColumns(Field field, String table, Map<String, String> overrides, Set<EnumColumn> into) {
        var enumerated = field.getAnnotation(Enumerated.class);
        var elementCollection = field.getAnnotation(ElementCollection.class);

        if (elementCollection != null) {
            var collectionTable = collectionTableOf(field, table);
            var elementType = elementTypeOf(field, elementCollection);
            if (elementType.isEnum()) {
                if (isStringEnumerated(enumerated)) {
                    into.add(new EnumColumn(collectionTable, columnNameOf(field, overrides), enumNames(elementType)));
                }
            } else if (elementType.isAnnotationPresent(Embeddable.class)) {
                collectEmbeddableColumns(elementType, collectionTable, overridesOf(field), into);
            }
            return;
        }

        if (field.isAnnotationPresent(Embedded.class) || field.getType().isAnnotationPresent(Embeddable.class)) {
            collectEmbeddableColumns(field.getType(), table, overridesOf(field), into);
            return;
        }

        if (field.getType().isEnum() && isStringEnumerated(enumerated)) {
            into.add(new EnumColumn(table, columnNameOf(field, overrides), enumNames(field.getType())));
        }
    }

    private static void collectEmbeddableColumns(Class<?> embeddable, String table, Map<String, String> overrides, Set<EnumColumn> into) {
        for (var field : persistentFields(embeddable)) {
            collectFieldColumns(field, table, overrides, into);
        }
    }

    private static boolean isStringEnumerated(Enumerated enumerated) {
        return enumerated != null && enumerated.value() == EnumType.STRING;
    }

    private static Set<String> enumNames(Class<?> enumType) {
        return Arrays.stream(enumType.getEnumConstants())
                .map(constant -> ((Enum<?>) constant).name())
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private static List<Field> persistentFields(Class<?> type) {
        var fields = new ArrayList<Field>();
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            if (current != type && current.isAnnotationPresent(Entity.class)) {
                break;
            }
            if (current != type && !current.isAnnotationPresent(MappedSuperclass.class) && !current.isAnnotationPresent(Embeddable.class)) {
                continue;
            }
            for (var field : current.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())
                        || field.isAnnotationPresent(Transient.class) || field.isSynthetic()) {
                    continue;
                }
                fields.add(field);
            }
        }
        return fields;
    }

    private static String tableOf(Class<?> entity) {
        var root = entity;
        for (Class<?> current = entity.getSuperclass(); current != null; current = current.getSuperclass()) {
            if (current.isAnnotationPresent(Entity.class)) {
                root = current;
            }
        }
        if (root != entity) {
            var inheritance = root.getAnnotation(Inheritance.class);
            var strategy = inheritance == null ? InheritanceType.SINGLE_TABLE : inheritance.strategy();
            if (strategy == InheritanceType.SINGLE_TABLE) {
                return ownTableOf(root);
            }
        }
        return ownTableOf(entity);
    }

    private static String ownTableOf(Class<?> entity) {
        var table = entity.getAnnotation(Table.class);
        if (table != null && !table.name().isBlank()) {
            return table.name();
        }
        var annotation = entity.getAnnotation(Entity.class);
        var entityName = annotation.name().isBlank() ? entity.getSimpleName() : annotation.name();
        return physical(entityName);
    }

    private static String collectionTableOf(Field field, String ownerTable) {
        var collectionTable = field.getAnnotation(CollectionTable.class);
        if (collectionTable != null && !collectionTable.name().isBlank()) {
            return collectionTable.name();
        }
        var joinTable = field.getAnnotation(JoinTable.class);
        if (joinTable != null && !joinTable.name().isBlank()) {
            return joinTable.name();
        }
        return physical(ownerTable + "_" + field.getName());
    }

    private static Class<?> elementTypeOf(Field field, ElementCollection elementCollection) {
        if (elementCollection.targetClass() != void.class) {
            return elementCollection.targetClass();
        }
        Type generic = field.getGenericType();
        if (generic instanceof ParameterizedType parameterized) {
            var arguments = parameterized.getActualTypeArguments();
            var elementArgument = Map.class.isAssignableFrom(field.getType()) ? arguments[arguments.length - 1] : arguments[0];
            if (elementArgument instanceof Class<?> elementClass) {
                return elementClass;
            }
        }
        throw new IllegalStateException("Cannot determine element type of " + field);
    }

    private static String columnNameOf(Field field, Map<String, String> overrides) {
        var overridden = overrides.get(field.getName());
        if (overridden != null) {
            return overridden;
        }
        var column = field.getAnnotation(Column.class);
        if (column != null && !column.name().isBlank()) {
            return column.name();
        }
        return physical(field.getName());
    }

    private static Map<String, String> overridesOf(Field field) {
        var overrides = new HashMap<String, String>();
        for (var override : field.getAnnotationsByType(AttributeOverride.class)) {
            if (!override.column().name().isBlank()) {
                overrides.put(override.name(), override.column().name());
            }
        }
        return overrides;
    }

    private static String physical(String logicalName) {
        return NAMING.toPhysicalTableName(Identifier.toIdentifier(logicalName), null).getText();
    }
}
