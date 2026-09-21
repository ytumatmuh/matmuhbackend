package com.matmuh.matmuhsite.core.properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FrontendPropertiesTest {

    private FrontendProperties properties;

    @BeforeEach
    void setUp() {
        properties = new FrontendProperties();
        properties.setCallbackUrl("https://matmuh.yusufacmaci.com/auth/callback");
        properties.setAllowedCallbacks(List.of("https://admin.matmuh.yusufacmaci.com/callback"));
    }

    @Test
    void acceptsExactMatchFromAllowedList() {
        assertTrue(properties.isAllowedCallback("https://admin.matmuh.yusufacmaci.com/callback"));
    }

    @Test
    void acceptsAnyPathOnTheDefaultCallbackOrigin() {
        assertTrue(properties.isAllowedCallback("https://matmuh.yusufacmaci.com/?cms-auth=done"));
        assertTrue(properties.isAllowedCallback("https://matmuh.yusufacmaci.com/duyurular?cms-auth=done"));
    }

    @Test
    void acceptsAnyPathOnAnAllowedOrigin() {
        assertTrue(properties.isAllowedCallback("https://admin.matmuh.yusufacmaci.com/panel?cms-auth=done"));
    }

    @Test
    void rejectsForeignOrigin() {
        assertFalse(properties.isAllowedCallback("https://saldirgan.com/steal"));
    }

    @Test
    void rejectsSuffixLookalikeDomain() {
        assertFalse(properties.isAllowedCallback("https://matmuh.yusufacmaci.com.saldirgan.com/steal"));
    }

    @Test
    void rejectsSchemeDowngrade() {
        assertFalse(properties.isAllowedCallback("http://matmuh.yusufacmaci.com/?cms-auth=done"));
    }

    @Test
    void rejectsBlankAndMalformed() {
        assertFalse(properties.isAllowedCallback(null));
        assertFalse(properties.isAllowedCallback(""));
        assertFalse(properties.isAllowedCallback("   "));
        assertFalse(properties.isAllowedCallback("/auth/callback"));
        assertFalse(properties.isAllowedCallback("javascript:alert(1)"));
    }
}
