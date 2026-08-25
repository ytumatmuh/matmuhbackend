package com.matmuh.matmuhsite.core.helpers;

public final class LogMasks {

    private LogMasks() {}


    public static String email(String email) {
        if (email == null || email.isBlank()) {
            return "-";
        }

        var at = email.indexOf('@');
        if (at <= 0) {
            return "***";
        }

        return email.charAt(0) + "***" + email.substring(at);
    }
}
