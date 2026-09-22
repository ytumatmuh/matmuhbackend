package com.matmuh.matmuhsite.core.helpers;

import com.matmuh.matmuhsite.core.helpers.EntityColumns.MappedColumn;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityColumnsTest {

    private static final List<String> COLUMNS = EntityColumns.discover().stream()
            .map(MappedColumn::qualifiedName)
            .toList();

    @Test
    void embeddedFieldsFollowTheirAttributeOverrides() {
        assertTrue(COLUMNS.contains("staff.photo_url"));
        assertTrue(COLUMNS.contains("staff.photo_alt"));
        assertFalse(COLUMNS.contains("staff.src"));
        assertFalse(COLUMNS.contains("staff.alt"));
    }

    @Test
    void elementCollectionsLandInTheirOwnTable() {
        assertTrue(COLUMNS.containsAll(List.of("lecture_syllabus.week", "lecture_syllabus.topic", "lecture_syllabus.topic_en")));
        assertTrue(COLUMNS.contains("authorities.role"));
        assertFalse(COLUMNS.contains("lectures.syllabus"));
    }

    @Test
    void mappedSuperclassColumnsStayAndAssociationsDoNot() {
        assertTrue(COLUMNS.contains("lectures.is_deleted"));
        assertTrue(COLUMNS.contains("lectures.created_at"));
        assertFalse(COLUMNS.contains("lectures.created_by"));
        assertFalse(COLUMNS.contains("lectures.offerings"));
    }
}
