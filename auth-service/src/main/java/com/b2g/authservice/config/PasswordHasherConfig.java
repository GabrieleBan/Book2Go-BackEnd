package com.b2g.authservice.config;

import com.b2g.authservice.service.PasswordHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordHasherConfig {

    @Autowired
    public PasswordHasherConfig(PasswordEncoder encoder) {
        PasswordHasher.configure(encoder);
    }
}