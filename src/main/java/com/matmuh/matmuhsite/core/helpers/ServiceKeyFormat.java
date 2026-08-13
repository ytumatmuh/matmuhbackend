package com.matmuh.matmuhsite.core.helpers;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;


public final class ServiceKeyFormat {

    public static final String PREFIX = "mtm_live_";

    private static final int LOOKUP_LENGTH = PREFIX.length() + 8;

    private static final SecureRandom RANDOM = new SecureRandom();

    private ServiceKeyFormat() {}

    public static boolean looksLikeServiceKey(String token) {
        return token != null && token.startsWith(PREFIX);
    }

    public static String generate() {
        var bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String lookupPrefix(String rawKey) {
        return rawKey.length() <= LOOKUP_LENGTH ? rawKey : rawKey.substring(0, LOOKUP_LENGTH);
    }

    public static String hash(String rawKey) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawKey.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean matches(String rawKey, String storedHash) {
        return MessageDigest.isEqual(
                hash(rawKey).getBytes(StandardCharsets.UTF_8),
                storedHash.getBytes(StandardCharsets.UTF_8));
    }
}
