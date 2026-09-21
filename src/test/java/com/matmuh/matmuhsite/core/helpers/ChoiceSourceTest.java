package com.matmuh.matmuhsite.core.helpers;

import com.matmuh.matmuhsite.core.dtos.cms.response.ChoiceSource;
import com.matmuh.matmuhsite.core.dtos.cms.response.FieldDefinition;
import com.matmuh.matmuhsite.entities.cms.FieldType;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChoiceSourceTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    // pointsToCollection() bir kez isCollection() adındaydı ve Jackson onu
    // `collection` alanı sanıp koleksiyon adını true/false ile ezdi.
    @Test
    void collectionSourceKeepsItsTargetOnTheWire() {
        var json = mapper.writeValueAsString(ChoiceSource.ofCollection("staff"));

        assertTrue(json.contains("\"collection\":\"staff\""), json);
        assertFalse(json.contains("true"), json);
    }

    @Test
    void staticSourceOmitsTheCollectionKey() {
        var json = mapper.writeValueAsString(ChoiceSource.ofValues("FALL", "SPRING"));

        assertTrue(json.contains("\"kind\":\"static\""), json);
        assertFalse(json.contains("collection"), json);
    }

    @Test
    void selectCarriesItsSource() {
        var field = FieldDefinition.select("semester", "Dönem", ChoiceSource.ofValues("FALL"));

        assertEquals(FieldType.SELECT, field.type());
        assertEquals("static", field.source().kind());
    }
}
