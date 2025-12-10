package com.b2g.authservice.service;
public class OAuthUserService {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    public User findOrCreateOAuthUser(String email, OAuthProvider provider) {
        return userRepository.findByEmail(email)
                .map(u -> updateUserOAuth(u, provider))
                .orElseGet(() -> createNewOAuthUser(email, provider));
    }

    private User updateUserOAuth(User user, OAuthProvider provider) {
        if (!user.isEnabled()) user.enable();
        if (user.getAuthProvider() == OAuthProvider.NONE) user.setAuthProvider(provider);
        return userRepository.save(user);
    }

    private User createNewOAuthUser(String email, OAuthProvider provider) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(generateUsernameFromEmail(email));
        user.setCredentials(new Credentials(passwordEncoder.encode(UUID.randomUUID().toString())));
        user.setRoles(Set.of(UserRole.READER));
        user.setAuthProvider(provider);
        user.enable();
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
}