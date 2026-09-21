package com.matmuh.matmuhsite.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RoleTest {

    @Test
    void acceptsShortName() {
        assertEquals(Role.ROLE_EDITOR, Role.fromValue("EDITOR"));
        assertEquals(Role.ROLE_ADMIN, Role.fromValue("ADMIN"));
        assertEquals(Role.ROLE_USER, Role.fromValue("USER"));
    }

    @Test
    void acceptsPrefixedName() {
        assertEquals(Role.ROLE_EDITOR, Role.fromValue("ROLE_EDITOR"));
    }

    @Test
    void isCaseAndWhitespaceInsensitive() {
        assertEquals(Role.ROLE_EDITOR, Role.fromValue("  editor "));
        assertEquals(Role.ROLE_EDITOR, Role.fromValue("role_Editor"));
    }

    @Test
    void returnsNullForUnknownOrBlank() {
        assertNull(Role.fromValue("MODERATOR"));
        assertNull(Role.fromValue(""));
        assertNull(Role.fromValue(null));
    }
}
