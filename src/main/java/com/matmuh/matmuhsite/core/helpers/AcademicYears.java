package com.matmuh.matmuhsite.core.helpers;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public final class AcademicYears {

    private AcademicYears() {}

    public static final int FIRST_YEAR = 2000;
    public static final int FUTURE_YEARS = 1;

    private static final Pattern FORMAT = Pattern.compile("^(\\d{4})-(\\d{4})$");

    public static boolean isValid(String value) {
        if (value == null) return false;
        var matcher = FORMAT.matcher(value.trim());
        if (!matcher.matches()) return false;

        var start = Integer.parseInt(matcher.group(1));
        var end = Integer.parseInt(matcher.group(2));

        return end == start + 1 && start >= FIRST_YEAR && start <= currentStartYear() + FUTURE_YEARS;
    }

    public static String current() {
        var start = currentStartYear();
        return start + "-" + (start + 1);
    }

    public static List<String> selectable() {
        var newest = currentStartYear() + FUTURE_YEARS;
        return IntStream.rangeClosed(FIRST_YEAR, newest)
                .map(year -> FIRST_YEAR + newest - year)
                .mapToObj(start -> start + "-" + (start + 1))
                .toList();
    }

    private static int currentStartYear() {
        var today = LocalDate.now();
        return today.getMonthValue() >= 8 ? today.getYear() : today.getYear() - 1;
    }
}
