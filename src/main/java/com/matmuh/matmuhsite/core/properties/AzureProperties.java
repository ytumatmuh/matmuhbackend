package com.matmuh.matmuhsite.core.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "spring.security.oauth2.client")
@Getter
@Setter
public class AzureProperties {

    private Map<String, Registration> registration;
    private Map<String, Provider> provider;

    @Getter
    @Setter
    public static class Registration {
        private String clientId;
        private String clientSecret;
        private String clientName;
        private List<String> scope;
        private String authorizationGrantType;
        private String clientAuthenticationMethod;
        private String redirectUri;
    }

    @Getter
    @Setter
    public static class Provider {
        private String authorizationUri;
        private String tokenUri;
        private String jwkSetUri;
        private String userNameAttribute;
    }
}