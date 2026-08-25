package com.matmuh.matmuhsite.core.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Configuration
@ConfigurationProperties(prefix = "academic")
@Getter
@Setter
public class AcademicProperties {

    private Set<String> departmentCodePrefixes = new LinkedHashSet<>(List.of("MTM"));

    public boolean isDepartmentCode(String lectureCode) {
        if (lectureCode == null) {
            return false;
        }

        var normalized = lectureCode.trim().toUpperCase(Locale.ROOT);
        return departmentCodePrefixes.stream()
                .anyMatch(prefix -> normalized.startsWith(prefix.toUpperCase(Locale.ROOT)));
    }
}
