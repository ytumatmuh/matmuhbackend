package com.matmuh.matmuhsite.core.helpers;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategySnakeCaseImpl;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Açılış runner'ları (enum CHECK'leri, TEXT kolon genişletme) aynı varlık yürüyüşünü paylaşsın
// diye burada. @MappedSuperclass, SINGLE_TABLE, @ElementCollection ve @AttributeOverride'lı
// @Embeddable yolları tek yerde: ikinci bir kopya bunların birinde sessizce bayatlardı.
public final class EntityColumns {

    public static final String ENTITY_PACKAGE = "com.matmuh.matmuhsite.entities";

    private static final PhysicalNamingStrategySnakeCaseImpl NAMING = new PhysicalNamingStrategySnakeCaseImpl();

    private EntityColumns() {}

    public record MappedColumn(String table, String column, Field field, Class<?> type, String columnDefinition) {

        public String qualifiedName() {
            return table + "." + column;
        }
    }

    public static List<MappedColumn> discover() {
        return discover(ENTITY_PACKAGE);
    }

    public static List<MappedColumn> discover(String basePackage) {
        var found = new LinkedHashSet<MappedColumn>();
        for (var entity : entityClasses(basePackage)) {
            collectEntityColumns(entity, found);
        }
        return found.stream()
                .sorted(Comparator.comparing(MappedColumn::table).thenComparing(MappedColumn::column))
                .toList();
    }

    private static List<Class<?>> entityClasses(String basePackage) {
        var scanner = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                return beanDefinition.getMetadata().isIndependent();
            }
        };
        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
        var classes = new ArrayList<Class<?>>();
        for (var definition : scanner.findCandidateComponents(basePackage)) {
            try {
                classes.add(Class.forName(definition.getBeanClassName()));
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Entity class vanished during scan: " + definition.getBeanClassName(), e);
            }
        }
        classes.sort(Comparator.comparing(Class::getName));
        return classes;
    }

    private static void collectEntityColumns(Class<?> entity, Set<MappedColumn> into) {
        var table = tableOf(entity);
        for (var field : persistentFields(entity)) {
            collectFieldColumns(field, table, Map.of(), into);
        }
    }

    private static void collectFieldColumns(Field field, String table, Map<String, Column> overrides, Set<MappedColumn> into) {
        if (isAssociation(field)) {
            return;
        }

        var elementCollection = field.getAnnotation(ElementCollection.class);
        if (elementCollection != null) {
            var collectionTable = collectionTableOf(field, table);
            var elementType = elementTypeOf(field, elementCollection);
            if (elementType.isAnnotationPresent(Embeddable.class)) {
                collectEmbeddableColumns(elementType, collectionTable, overridesOf(field), into);
            } else {
                into.add(mappedColumn(collectionTable, field, elementType, overrides));
            }
            return;
        }

        if (field.isAnnotationPresent(Embedded.class) || field.getType().isAnnotationPresent(Embeddable.class)) {
            collectEmbeddableColumns(field.getType(), table, overridesOf(field), into);
            return;
        }

        into.add(mappedColumn(table, field, field.getType(), overrides));
    }

    private static void collectEmbeddableColumns(Class<?> embeddable, String table, Map<String, Column> overrides, Set<MappedColumn> into) {
        for (var field : persistentFields(embeddable)) {
            collectFieldColumns(field, table, overrides, into);
        }
    }

    private static MappedColumn mappedColumn(String table, Field field, Class<?> type, Map<String, Column> overrides) {
        var mapping = mappingOf(field, overrides);
        return new MappedColumn(table, columnNameOf(field, mapping), field, type, definitionOf(mapping));
    }

    private static boolean isAssociation(Field field) {
        return field.isAnnotationPresent(ManyToOne.class)
                || field.isAnnotationPresent(OneToOne.class)
                || field.isAnnotationPresent(OneToMany.class)
                || field.isAnnotationPresent(ManyToMany.class);
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

    private static Column mappingOf(Field field, Map<String, Column> overrides) {
        var overridden = overrides.get(field.getName());
        return overridden != null ? overridden : field.getAnnotation(Column.class);
    }

    private static String columnNameOf(Field field, Column mapping) {
        if (mapping != null && !mapping.name().isBlank()) {
            return mapping.name();
        }
        return physical(field.getName());
    }

    private static String definitionOf(Column mapping) {
        return mapping == null ? "" : mapping.columnDefinition().trim();
    }

    private static Map<String, Column> overridesOf(Field field) {
        var overrides = new HashMap<String, Column>();
        for (var override : field.getAnnotationsByType(AttributeOverride.class)) {
            if (!override.column().name().isBlank()) {
                overrides.put(override.name(), override.column());
            }
        }
        return overrides;
    }

    private static String physical(String logicalName) {
        return NAMING.toPhysicalTableName(Identifier.toIdentifier(logicalName), null).getText();
    }
}
