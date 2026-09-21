package com.matmuh.matmuhsite.core.helpers;

import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionSchema;
import com.matmuh.matmuhsite.core.dtos.cms.response.FieldDefinition;
import com.matmuh.matmuhsite.core.exceptions.CmsValidationException;
import com.matmuh.matmuhsite.entities.cms.FieldType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CollectionSortParserTest {

    private final CollectionSchema schema = CollectionSchema.of(
            FieldDefinition.of("title", FieldType.SHORT_TEXT, "Başlık"),
            FieldDefinition.of("featured", FieldType.BOOL, "Öne çıkan").asSortable(),
            FieldDefinition.of("publishedAt", FieldType.DATE, "Yayın tarihi").asSortable()
    );

    @Test
    void defaultsToSlugAscending() {
        var sorts = CollectionSortParser.parse(schema, null);

        assertEquals(1, sorts.size());
        assertEquals("slug", sorts.get(0).column());
        assertFalse(sorts.get(0).descending());
        assertFalse(sorts.get(0).isDataField());
    }

    @Test
    void mapsBuiltInColumns() {
        assertEquals("created_at", CollectionSortParser.parse(schema, "createdAt").get(0).column());
        assertEquals("updated_at", CollectionSortParser.parse(schema, "updatedAt:desc").get(0).column());
        assertTrue(CollectionSortParser.parse(schema, "updatedAt:desc").get(0).descending());
    }

    @Test
    void acceptsSortableSchemaField() {
        var sort = CollectionSortParser.parse(schema, "publishedAt:desc").get(0);

        assertTrue(sort.isDataField());
        assertEquals("publishedAt", sort.dataField());
        assertTrue(sort.descending());
        assertNull(sort.column());
    }

    @Test
    void keepsTheOrderOfSeveralSortTerms() {
        var sorts = CollectionSortParser.parse(schema, "featured:desc,publishedAt:desc");

        assertEquals(2, sorts.size());
        assertEquals("featured", sorts.get(0).dataField());
        assertTrue(sorts.get(0).descending());
        assertEquals("publishedAt", sorts.get(1).dataField());
        assertTrue(sorts.get(1).descending());
    }

    @Test
    void mixesSchemaFieldsWithBuiltInColumns() {
        var sorts = CollectionSortParser.parse(schema, "featured:desc, createdAt:asc");

        assertEquals(2, sorts.size());
        assertEquals("featured", sorts.get(0).dataField());
        assertEquals("created_at", sorts.get(1).column());
        assertFalse(sorts.get(1).descending());
    }

    @Test
    void rejectsTooManySortFields() {
        assertThrows(CmsValidationException.class,
                () -> CollectionSortParser.parse(schema, "featured:desc,publishedAt:desc,createdAt,updatedAt"));
    }

    @Test
    void rejectsTheSameFieldTwice() {
        assertThrows(CmsValidationException.class,
                () -> CollectionSortParser.parse(schema, "publishedAt:desc,publishedAt:asc"));
    }

    @Test
    void rejectsAnUnsortableFieldAnywhereInTheList() {
        assertThrows(CmsValidationException.class,
                () -> CollectionSortParser.parse(schema, "featured:desc,title"));
    }

    @Test
    void rejectsFieldNotMarkedSortable() {
        assertThrows(CmsValidationException.class, () -> CollectionSortParser.parse(schema, "title"));
    }

    @Test
    void rejectsUnknownField() {
        assertThrows(CmsValidationException.class, () -> CollectionSortParser.parse(schema, "hiçYok"));
    }

    @Test
    void rejectsSqlLookingInput() {
        assertThrows(CmsValidationException.class,
                () -> CollectionSortParser.parse(schema, "slug; DROP TABLE collection_items"));
        assertThrows(CmsValidationException.class,
                () -> CollectionSortParser.parse(schema, "publishedAt:desc; DELETE FROM users"));
        assertThrows(CmsValidationException.class,
                () -> CollectionSortParser.parse(schema, "publishedAt:desc,slug; DROP TABLE collection_items"));
    }

    @Test
    void rejectsBadDirectionAndShape() {
        assertThrows(CmsValidationException.class, () -> CollectionSortParser.parse(schema, "slug:sideways"));
        assertThrows(CmsValidationException.class, () -> CollectionSortParser.parse(schema, "slug:asc:desc"));
    }
}
