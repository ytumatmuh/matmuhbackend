package com.matmuh.matmuhsite.core.helpers;

import com.matmuh.matmuhsite.core.properties.CookieProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AuthCookieFactory {

    public static final String ACCESS_COOKIE = "access_token";
    public static final String REFRESH_COOKIE = "refresh_token";
    public static final String REFRESH_PATH = "/api/auth";

    private final CookieProperties cookieProperties;

    @Value("${jwt.refresh-validity-days:30}")
    private long refreshValidityDays;

    public AuthCookieFactory(CookieProperties cookieProperties) {
        this.cookieProperties = cookieProperties;
    }

    public String access(String value, long maxAgeSeconds) {
        return build(ACCESS_COOKIE, value, "/", maxAgeSeconds);
    }

    public String refresh(String value) {
        return build(REFRESH_COOKIE, value, REFRESH_PATH, refreshValidityDays * 24 * 60 * 60);
    }

    public String clearedAccess() {
        return build(ACCESS_COOKIE, "", "/", 0);
    }

    public String clearedRefresh() {
        return build(REFRESH_COOKIE, "", REFRESH_PATH, 0);
    }

    private String build(String name, String value, String path, long maxAgeSeconds) {
        return name + "=" + value
                + "; Path=" + path
                + "; HttpOnly"
                + (cookieProperties.isSecure() ? "; Secure" : "")
                + "; SameSite=" + cookieProperties.getSameSite()
                + "; Max-Age=" + maxAgeSeconds;
    }
}
