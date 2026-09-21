package com.matmuh.matmuhsite.core.helpers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceKeyFormatTest {

    @Test
    void generatedKeysCarryThePrefixAndAreUnique() {
        var first = ServiceKeyFormat.generate();
        var second = ServiceKeyFormat.generate();

        assertTrue(first.startsWith("mtm_live_"));
        assertNotEquals(first, second);
    }

    @Test
    void jwtIsNeverMistakenForAServiceKey() {
        // JWT'ler "eyJ" ile başlar; iki kimlik türü aynı Authorization kanalını
        // paylaştığı için bu ayrımın tutması şart.
        assertFalse(ServiceKeyFormat.looksLikeServiceKey("eyJhbGciOiJIUzI1NiJ9.abc.def"));
        assertFalse(ServiceKeyFormat.looksLikeServiceKey(null));
        assertTrue(ServiceKeyFormat.looksLikeServiceKey(ServiceKeyFormat.generate()));
    }

    @Test
    void lookupPrefixIsStableAndShared() {
        var key = ServiceKeyFormat.generate();

        assertEquals(ServiceKeyFormat.lookupPrefix(key), ServiceKeyFormat.lookupPrefix(key));
        assertTrue(key.startsWith(ServiceKeyFormat.lookupPrefix(key)));
        assertTrue(ServiceKeyFormat.lookupPrefix(key).length() < key.length());
    }

    @Test
    void hashMatchesOnlyTheOriginalKey() {
        var key = ServiceKeyFormat.generate();
        var hash = ServiceKeyFormat.hash(key);

        assertEquals(64, hash.length());
        assertTrue(ServiceKeyFormat.matches(key, hash));
        assertFalse(ServiceKeyFormat.matches(ServiceKeyFormat.generate(), hash));
    }
}
