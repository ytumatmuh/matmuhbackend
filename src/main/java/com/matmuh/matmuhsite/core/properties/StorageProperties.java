package com.matmuh.matmuhsite.core.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "storage")
@Getter
@Setter
public class StorageProperties {

    private String type = "local";

    private String bucket;

    private String region = "eu-central-1";

    private String accessKey;

    private String secretKey;

    private String endpoint;

    private int signedUrlMinutes = 15;

    private Local local = new Local();

    @Getter
    @Setter
    public static class Local {
        private String path = "uploads";
    }

    public boolean hasStaticCredentials() {
        return accessKey != null && !accessKey.isBlank() && secretKey != null && !secretKey.isBlank();
    }

    public boolean hasCustomEndpoint() {
        return endpoint != null && !endpoint.isBlank();
    }
}
