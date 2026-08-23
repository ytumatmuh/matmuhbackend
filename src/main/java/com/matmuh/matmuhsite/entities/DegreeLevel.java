package com.matmuh.matmuhsite.entities;

import java.util.Set;
import java.util.regex.Pattern;

public enum DegreeLevel {
    UNDERGRADUATE,
    MASTERS,
    DOCTORATE;

    private static final Pattern COURSE_NUMBER = Pattern.compile("(\\d{4})");

    private static final Set<DegreeLevel> GRADUATE = Set.of(MASTERS, DOCTORATE);

    private static final int FIRST_UNDERGRADUATE_NUMBER = 1000;
    private static final int FIRST_MASTERS_NUMBER = 5000;
    private static final int FIRST_DOCTORATE_NUMBER = 6000;
    private static final int PROGRAM_SPECIFIC_BLOCK = 4;

    public static Set<DegreeLevel> fromCode(String code) {
        var number = courseNumber(code);
        if (number == null || number < FIRST_UNDERGRADUATE_NUMBER) {
            return Set.of();
        }

        if (number < FIRST_MASTERS_NUMBER) {
            return Set.of(UNDERGRADUATE);
        }

        if (number < FIRST_MASTERS_NUMBER + PROGRAM_SPECIFIC_BLOCK) {
            return Set.of(MASTERS);
        }

        if (number >= FIRST_DOCTORATE_NUMBER && number < FIRST_DOCTORATE_NUMBER + PROGRAM_SPECIFIC_BLOCK) {
            return Set.of(DOCTORATE);
        }

        return GRADUATE;
    }

    private static Integer courseNumber(String code) {
        if (code == null) {
            return null;
        }

        var matcher = COURSE_NUMBER.matcher(code);
        return matcher.find() ? Integer.valueOf(matcher.group(1)) : null;
    }
}
