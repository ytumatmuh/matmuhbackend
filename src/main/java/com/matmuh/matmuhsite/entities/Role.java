package com.matmuh.matmuhsite.entities;

import org.springframework.security.core.GrantedAuthority;

import java.util.Locale;

public enum Role implements GrantedAuthority {

    ROLE_ADMIN("ADMIN"),
    ROLE_EDITOR("EDITOR"),
    ROLE_USER("USER");

    private String value;

    Role(String value) {
        this.value = value;
    }

    public String getValue(){
        return value;
    }

    @Override
    public String getAuthority() {
        return name();
    }

    public static Role fromValue(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        var normalized = raw.trim().toUpperCase(Locale.ROOT);
        if (!normalized.startsWith("ROLE_")) {
            normalized = "ROLE_" + normalized;
        }
        for (Role role : values()) {
            if (role.name().equals(normalized)) {
                return role;
            }
        }
        return null;
    }
}
