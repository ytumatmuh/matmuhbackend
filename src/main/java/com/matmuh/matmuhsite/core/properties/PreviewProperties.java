package com.matmuh.matmuhsite.core.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Configuration
@ConfigurationProperties(prefix = "preview")
@Getter
@Setter
public class PreviewProperties {

    private String converterUrl = "";

    private int timeoutSeconds = 30;

    private Set<String> convertibleExtensions = new LinkedHashSet<>(List.of(
            "doc", "docx", "xls", "xlsx", "ppt", "pptx"));

    public boolean isEnabled() {
        return converterUrl != null && !converterUrl.isBlank();
    }

    public boolean isConvertible(String extension) {
        return extension != null && convertibleExtensions.contains(extension);
    }
}
