package com.matmuh.matmuhsite.core.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.cookie")
@Getter
@Setter
public class CookieProperties {

    private String sameSite = "Lax";

    private boolean secure = true;

    public boolean allowsCookieWrites() {
        return "Lax".equalsIgnoreCase(sameSite) || "Strict".equalsIgnoreCase(sameSite);
    }
}
