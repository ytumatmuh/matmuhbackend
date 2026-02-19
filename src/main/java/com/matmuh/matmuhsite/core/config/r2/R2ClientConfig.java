package com.matmuh.matmuhsite.core.config.r2;

import com.matmuh.matmuhsite.core.properties.R2Properties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
public class R2ClientConfig {

    private final com.matmuh.matmuhsite.core.properties.R2Properties r2Properties;

    public R2ClientConfig(R2Properties r2Properties) {
        this.r2Properties = r2Properties;
    }

    @Bean
    public S3Client createS3Client(){

        return S3Client.builder()
                .endpointOverride(URI.create(r2Properties.getEndpoint()))
                .region(Region.of("auto"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(r2Properties.getAccessKey(), r2Properties.getSecretKey())
                ))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .chunkedEncodingEnabled(false)
                        .build())
                .build();
    }


}
