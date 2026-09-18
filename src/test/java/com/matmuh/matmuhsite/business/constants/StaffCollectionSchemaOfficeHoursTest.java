package com.matmuh.matmuhsite.business.constants;

import com.matmuh.matmuhsite.core.dtos.cms.response.FieldDefinition;
import com.matmuh.matmuhsite.core.helpers.CollectionSchemaValidator;
import com.matmuh.matmuhsite.entities.cms.FieldType;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaffCollectionSchemaOfficeHoursTest {

    @Test
    void officeHoursIsAnObjectArrayForThePanel() {
        var officeHours = StaffCollectionSchema.SCHEMA.fields().stream()
                .filter(field -> "officeHours".equals(field.name()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("staff schema must expose officeHours"));

        assertEquals(FieldType.OBJECT_ARRAY, officeHours.type());
        assertEquals("Görüşme Saatleri", officeHours.label());
        assertFalse(officeHours.required());

        var itemNames = officeHours.itemFields().stream().map(FieldDefinition::name).toList();
        assertEquals(List.of("dayOfWeek", "startTime", "endTime", "description"), itemNames);

        var day = officeHours.itemFields().get(0);
        assertEquals(FieldType.SELECT, day.type());
        assertTrue(day.required());
        assertTrue(day.source().values().contains("MONDAY"));

        assertEquals(FieldType.SHORT_TEXT, officeHours.itemFields().get(1).type());
        assertTrue(officeHours.itemFields().get(1).required());
        assertEquals(FieldType.SHORT_TEXT, officeHours.itemFields().get(2).type());
        assertTrue(officeHours.itemFields().get(2).required());
        assertEquals("description", officeHours.itemFields().get(3).name());
        assertFalse(officeHours.itemFields().get(3).required());
    }

    @Test
    void staffCollectionIsNotLocalized() {
        var def = new CollectionRegistry().resolve(StaffCollectionSchema.KEY);
        assertFalse(def.localized());
    }

    @Test
    void schemaKeepsOfficeHoursOnWrite() {
        var cleaned = CollectionSchemaValidator.validateAndStrip(StaffCollectionSchema.SCHEMA, JsonMapper.builder().build().readTree("""
                {
                  "firstName": "Ayşe",
                  "lastName": "Yılmaz",
                  "groups": ["ACADEMIC"],
                  "officeHours": [
                    {"dayOfWeek": "MONDAY", "startTime": "10:00", "endTime": "12:00", "description": "Ofis D-105"}
                  ]
                }
                """));
        assertEquals(1, cleaned.get("officeHours").size());
        assertEquals("MONDAY", cleaned.get("officeHours").get(0).get("dayOfWeek").asString());
        assertEquals("10:00", cleaned.get("officeHours").get(0).get("startTime").asString());
    }
}
