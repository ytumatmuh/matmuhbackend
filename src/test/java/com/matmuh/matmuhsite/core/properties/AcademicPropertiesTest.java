package com.matmuh.matmuhsite.core.properties;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AcademicPropertiesTest {

    private final AcademicProperties properties = new AcademicProperties();

    @Test
    void departmentCodeIsRecognised() {
        assertTrue(properties.isDepartmentCode("MTM1501"));
        assertTrue(properties.isDepartmentCode("mtm1501"));
        assertTrue(properties.isDepartmentCode("  MTM2011  "));
    }

    @Test
    void serviceCoursesAreNotDepartmentCodes() {
        assertFalse(properties.isDepartmentCode("FIZ1001"));
        assertFalse(properties.isDepartmentCode("ATA1031"));
        assertFalse(properties.isDepartmentCode("TDB1031"));
        assertFalse(properties.isDepartmentCode("MDB1051"));
    }

    @Test
    void missingCodeIsNotDepartmentCode() {
        assertFalse(properties.isDepartmentCode(null));
        assertFalse(properties.isDepartmentCode(""));
    }

    // Türkçe locale'de "iis".toUpperCase() İ üretir; önek karşılaştırması buna düşmemeli.
    @Test
    void prefixMatchingSurvivesTurkishLocale() {
        var previous = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr", "TR"));

            var withDottedPrefix = new AcademicProperties();
            withDottedPrefix.setDepartmentCodePrefixes(new LinkedHashSet<>(List.of("bil")));

            assertTrue(withDottedPrefix.isDepartmentCode("BIL1001"));
            assertTrue(withDottedPrefix.isDepartmentCode("bil1001"));
        } finally {
            Locale.setDefault(previous);
        }
    }
}
