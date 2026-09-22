package com.matmuh.matmuhsite.core.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Kodda "{anahtar}" diye geçen her doğrulama mesajının iki dilde karşılığı olmalı; yoksa
// istemci ham "{import.rows.not.empty}" görür (Egehan'ın botu bunu gördü, 22 Eylül).
class ValidationMessageKeysTest {

    private static final Pattern PLACEHOLDER = Pattern.compile("\"\\{([a-zA-Z0-9.]+)}\"");

    @Test
    void everyPlaceholderInCodeHasATurkishAndEnglishMessage() throws IOException {
        var used = new TreeSet<String>();
        try (Stream<Path> files = Files.walk(Path.of("src/main/java"))) {
            for (var file : files.filter(f -> f.toString().endsWith(".java")).toList()) {
                var matcher = PLACEHOLDER.matcher(Files.readString(file));
                while (matcher.find()) {
                    used.add(matcher.group(1));
                }
            }
        }

        for (var bundle : new String[]{"messages.properties", "messages_en.properties"}) {
            var properties = new Properties();
            try (var in = Files.newInputStream(Path.of("src/main/resources", bundle))) {
                properties.load(in);
            }
            var missing = new TreeSet<>(used);
            missing.removeAll(properties.stringPropertyNames());
            assertEquals(new TreeSet<String>(), missing, bundle + " eksik anahtarlar");
        }
    }
}
