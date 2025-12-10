package com.b2g.authservice.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Embedded
    private Credentials credentials;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Set<UserRole> roles = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OAuthProvider authProvider;

    @Column(nullable = false)
    private boolean enabled = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public void enable() { this.enabled = true; }
    public void disable() { this.enabled = false; }
    public void addRole(UserRole role) { this.roles.add(role); }
    public void removeRole(UserRole role) { this.roles.remove(role); }

    public boolean hasRole(UserRole role) { return roles.contains(role); }
    public boolean verifyPassword(String rawPassword) {
        return credentials.matches(rawPassword);
    }

}

