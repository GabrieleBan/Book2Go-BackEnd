package com.b2g.authservice.service;

import org.springframework.security.crypto.password.PasswordEncoder;

public final class PasswordHasher {

    private static PasswordEncoder encoder;

    private PasswordHasher() {}

    public static void configure(PasswordEncoder passwordEncoder) {
        encoder = passwordEncoder;
    }

    public static String hash(String raw) {
        if (encoder == null) {
            throw new IllegalStateException("PasswordHasher not configured");
        }
        return encoder.encode(raw);
    }

    public static boolean matches(String raw, String hashed) {
        if (encoder == null) {
            throw new IllegalStateException("PasswordHasher not configured");
        }
        return encoder.matches(raw, hashed);
    }
}