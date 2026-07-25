package com.matmuh.matmuhsite.core.helpers;

import java.util.Map;

public final class SlugGenerator {

    private SlugGenerator() {}

    private static final int MAX_LENGTH = 80;

    private static final Map<Character, Character> TURKISH_MAP = Map.ofEntries(
            Map.entry('ç', 'c'), Map.entry('Ç', 'c'),
            Map.entry('ğ', 'g'), Map.entry('Ğ', 'g'),
            Map.entry('ı', 'i'), Map.entry('I', 'i'),
            Map.entry('İ', 'i'),
            Map.entry('ö', 'o'), Map.entry('Ö', 'o'),
            Map.entry('ş', 's'), Map.entry('Ş', 's'),
            Map.entry('ü', 'u'), Map.entry('Ü', 'u')
    );

    public static String slugify(String input) {
        if (input == null || input.isBlank()) return "";

        var sb = new StringBuilder(input.length());
        for (char ch : input.toCharArray()) {
            char c = TURKISH_MAP.getOrDefault(ch, Character.toLowerCase(ch));
            if (Character.isLetterOrDigit(c)) {
                sb.append(c);
            } else if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '-') {
                sb.append('-');
            }
        }

        var result = sb.toString();
        while (result.startsWith("-")) result = result.substring(1);
        while (result.endsWith("-")) result = result.substring(0, result.length() - 1);

        if (result.length() > MAX_LENGTH) {
            result = result.substring(0, MAX_LENGTH);
            while (result.endsWith("-")) result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}