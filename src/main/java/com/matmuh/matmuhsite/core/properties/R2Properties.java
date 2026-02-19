package com.matmuh.matmuhsite.core.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "r2")
@Getter
@Setter
public class R2Properties {
    private String endpoint;
    private String publicUrl;
    private String accessKey;
    private String secretKey;
    private String bucketName;
}