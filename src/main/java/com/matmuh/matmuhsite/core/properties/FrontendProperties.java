package com.matmuh.matmuhsite.core.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app.frontend")
@Getter
@Setter
public class FrontendProperties {

    private String callbackUrl;

    private List<String> allowedCallbacks = new ArrayList<>();

    public boolean isAllowedCallback(String url) {
        return url != null && !url.isBlank() && allowedCallbacks.contains(url);
    }
}
