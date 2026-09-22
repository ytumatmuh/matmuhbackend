package com.matmuh.matmuhsite.core.utilities.schema;

import com.matmuh.matmuhsite.core.helpers.EntityColumns.MappedColumn;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TextColumnWidenerTest {

    // Liste literal: yeni bir @Column(columnDefinition = "TEXT") alanı bu testi kırar, böylece
    // kolonun prod'da gerçekten text olduğu bilerek doğrulanır.
    @Test
    void discoversEveryTextColumnFromTheEntities() {
        var expected = List.of(
                "elective_groups.about",
                "elective_groups.about_en",
                "lecture_syllabus.topic",
                "lecture_syllabus.topic_en",
                "lectures.about",
                "lectures.about_en",
                "lectures.grading_policy",
                "lectures.grading_policy_en",
                "lectures.resources",
                "lectures.resources_en"
        );

        assertEquals(expected, qualifiedNames());
    }

    // jsonb kolonları da columnDefinition taşıyor; text'e çevrilmeleri veriyi bozardı.
    @Test
    void jsonbColumnsAreLeftAlone() {
        assertFalse(qualifiedNames().contains("content_blocks.value"));
        assertFalse(qualifiedNames().contains("collection_items.data"));
        assertFalse(qualifiedNames().contains("content_drafts.payload"));
    }

    private static List<String> qualifiedNames() {
        return TextColumnWidener.discover().stream().map(MappedColumn::qualifiedName).toList();
    }
}
