package com.b2g.authservice.service;

import com.b2g.authservice.model.Credentials;
import com.b2g.authservice.model.OAuthProvider;
import com.b2g.authservice.model.User;
import com.b2g.authservice.model.UserRole;
import com.b2g.authservice.repository.UserRepository;
import com.b2g.commons.UserRegistrationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthUserService {

    private final UserRepository userRepository;
    private final OAuth2AuthorizedClientService clientService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.routing-key.user-confirmed}")
    private String userConfirmedRoutingKey;

    private static final String GOOGLE_PROVIDER = "google";
    private static final String GITHUB_PROVIDER = "github";
    private static final String GITHUB_API_BASE_URL = "https://api.github.com";
    private static final String USER_EMAILS_ENDPOINT = "/user/emails";

    public boolean isValidProvider(String registrationId) {
        return GOOGLE_PROVIDER.equals(registrationId) || GITHUB_PROVIDER.equals(registrationId);
    }

    public User findOrCreateUser(OAuth2AuthenticationToken authentication) {
        String email = extractEmail(authentication);
        String provider = authentication.getAuthorizedClientRegistrationId();

        return userRepository.findByEmail(email)
                .map(user -> updateExistingUser(user, provider))
                .orElseGet(() -> createNewUser(email, provider));
    }

    private User updateExistingUser(User user, String provider) {
        boolean updated = false;
        if (!user.isEnabled()) {
            user.enable();
            updated = true;
        }

        if (user.getAuthProvider() == OAuthProvider.NONE) {
            user.setAuthProvider(OAuthProvider.valueOf(provider.toUpperCase()));
            updated = true;
        }

        if (updated) {
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }
        return user;
    }

    private User createNewUser(String email, String provider) {
        User user = User.builder()
                .username(generateUsernameFromEmail(email))
                .email(email)
                .credentials(Credentials.fromRawPassword(UUID.randomUUID().toString()))
                .roles(Set.of(UserRole.READER))
                .enabled(true)
                .authProvider(OAuthProvider.valueOf(provider.toUpperCase()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);

        UserRegistrationMessage msg = UserRegistrationMessage.builder()
                .username(user.getUsername())
                .uuid(user.getId())
                .email(user.getEmail())
                .build();

        rabbitTemplate.convertAndSend(exchangeName, userConfirmedRoutingKey, msg);

        return user;
    }

    private String extractEmail(OAuth2AuthenticationToken authentication) {
        Map<String, Object> attributes = authentication.getPrincipal().getAttributes();
        String email = (String) attributes.get("email");

        if (email == null && GITHUB_PROVIDER.equals(authentication.getAuthorizedClientRegistrationId())) {
            email = fetchGithubPrimaryEmail(authentication);
        }

        return email;
    }

    private String fetchGithubPrimaryEmail(OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );

        WebClient webClient = WebClient.builder()
                .baseUrl(GITHUB_API_BASE_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + client.getAccessToken().getTokenValue())
                .build();

        List<Map<String, Object>> emails = webClient.get()
                .uri(USER_EMAILS_ENDPOINT)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .block();

        return emails.stream()
                .filter(e -> Boolean.TRUE.equals(e.get("primary")))
                .map(e -> (String) e.get("email"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("GitHub email not found"));
    }

    private String generateUsernameFromEmail(String email) {
        String base = email.substring(0, email.indexOf("@"));
        String username = base;
        int counter = 1;
        while (userRepository.findByUsername(username).isPresent()) {
            username = base + counter++;
        }
        return username;
    }
}