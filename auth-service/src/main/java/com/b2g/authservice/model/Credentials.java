package com.b2g.authservice.model;

import com.b2g.authservice.service.PasswordHasher;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
public class Credentials {

    @Column(name = "password_hash")
    private String passwordHash;

    /**
     * Costruisce un oggetto Credentials da una password raw
     * Hash automatico interno
     */
    public static Credentials fromRawPassword(String rawPassword) {
        Credentials c = new Credentials();
        c.passwordHash = PasswordHasher.hash(rawPassword);
        return c;
    }

    /**
     * Verifica se la password raw corrisponde all'hash
     */
    public boolean matches(String rawPassword) {
        return PasswordHasher.matches(rawPassword, this.passwordHash);
    }


}