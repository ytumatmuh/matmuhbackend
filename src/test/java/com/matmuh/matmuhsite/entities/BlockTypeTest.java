package com.matmuh.matmuhsite.entities;

import com.matmuh.matmuhsite.entities.cms.BlockType;
import com.matmuh.matmuhsite.entities.cms.FieldType;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BlockTypeTest {

    // Prod'da sync bu satırda patlıyordu: manifest ObjectArray gönderiyor,
    // enum tanımıyordu.
    @Test
    void objectArrayIsAccepted() {
        assertEquals(BlockType.OBJECT_ARRAY, BlockType.fromWireName("ObjectArray"));
    }

    @Test
    void everyContractTypeResolves() {
        for (var wire : new String[]{"ShortText", "LongText", "RichText", "Number", "Bool", "Url",
                "Date", "Image", "File", "Link", "Select", "StringArray", "ObjectArray", "Collection"}) {
            assertEquals(wire, BlockType.fromWireName(wire).getWireName(), wire);
        }
    }

    // 4.4.0: File. BlockType ile FieldType ayrı yerde yaşıyor, biri güncellenince
    // diğeri unutuluyor; ikisi aynı sözcük dağarcığını taşımalı.
    @Test
    void fieldTypeSpeaksTheSameVocabulary() {
        for (var type : FieldType.values()) {
            assertEquals(type.getWireName(), BlockType.fromWireName(type.getWireName()).getWireName(), type.name());
        }
        assertEquals(FieldType.FILE, FieldType.fromWireName("File"));
    }

    // Eski adlar okunmaya devam eder ama telde üretilmez.
    @Test
    void legacyNamesNormalise() {
        assertEquals(BlockType.OBJECT_ARRAY, BlockType.fromWireName("List"));
        assertEquals(BlockType.LONG_TEXT, BlockType.fromWireName("Text"));
    }

    @Test
    void unknownTypeStillFails() {
        assertThrows(IllegalArgumentException.class, () -> BlockType.fromWireName("Nope"));
    }

    // Türkçe locale'de "Link".toUpperCase() İ üretir; eşleştirme buna düşmemeli.
    @Test
    void matchingSurvivesTurkishLocale() {
        var previous = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr", "TR"));
            assertEquals(BlockType.LINK, BlockType.fromWireName("link"));
            assertEquals(BlockType.IMAGE, BlockType.fromWireName("IMAGE"));
        } finally {
            Locale.setDefault(previous);
        }
    }
}
