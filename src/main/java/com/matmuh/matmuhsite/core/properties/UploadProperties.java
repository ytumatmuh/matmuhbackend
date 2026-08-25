package com.matmuh.matmuhsite.core.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Configuration
@ConfigurationProperties(prefix = "upload")
@Getter
@Setter
public class UploadProperties {

    private int maxFileSizeMb = 25;

    private int maxImageSizeMb = 10;

    private int maxPendingNotesPerUser = 10;

    private Set<String> fileExtensions = new LinkedHashSet<>(List.of(
            "pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx",
            "txt", "md", "zip", "rar", "7z", "png", "jpg", "jpeg", "webp"));



    private Set<String> publicFileExtensions = new LinkedHashSet<>(List.of(
            "pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "txt", "png", "jpg", "jpeg", "webp"));

    private Set<String> imageExtensions = new LinkedHashSet<>(List.of(
            "png", "jpg", "jpeg", "webp", "gif"));
}
