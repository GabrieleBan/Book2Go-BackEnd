package com.b2g.authservice.service;

import com.b2g.authservice.dto.SignupRequest;
import com.b2g.authservice.model.Credentials;
import com.b2g.authservice.model.OAuthProvider;
import com.b2g.authservice.model.User;
import com.b2g.authservice.model.UserRole;
import com.b2g.authservice.repository.UserRepository;
import com.b2g.commons.UserRegistrationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitTemplate safeRabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.routing-key.signup-ticket}")
    private String userRegisteredRoutingKey;

    @Value("${app.rabbitmq.routing-key.user-confirmed}")
    private String userConfirmedRoutingKey;

    public User registerUser(SignupRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .credentials(new Credentials(request.getPassword()))
                .roles(Set.of(UserRole.READER))
                .enabled(false)
                .authProvider(OAuthProvider.NONE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);

        sendUserRegisteredMessage(user);

        return user;
    }

    public void confirmEmail(User user) {
        user.enable();
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        sendUserConfirmedMessage(user);
    }

    private void sendUserRegisteredMessage(User user) {
        UserRegistrationMessage message = UserRegistrationMessage.builder()
                .username(user.getUsername())
                .uuid(user.getId())
                .email(user.getEmail())
                .build();

        try {
            safeRabbitTemplate.invoke(ops -> {
                ops.execute(channel -> {
                    byte[] body = safeRabbitTemplate.getMessageConverter()
                            .toMessage(message, new MessageProperties())
                            .getBody();
                    channel.basicPublish(
                            exchangeName,
                            userRegisteredRoutingKey,
                            true,
                            null,
                            body
                    );
                    return null;
                });
                ops.waitForConfirmsOrDie(3000);
                return null;
            });
        } catch (Exception e) {
            log.error("❌ Errore nella consegna del messaggio di registrazione", e);
            userRepository.delete(user);
            throw new IllegalStateException("Registrazione fallita: MQ non disponibile");
        }
    }

    private void sendUserConfirmedMessage(User user) {
        UserRegistrationMessage message = UserRegistrationMessage.builder()
                .username(user.getUsername())
                .uuid(user.getId())
                .email(user.getEmail())
                .build();

        rabbitTemplate.convertAndSend(exchangeName, userConfirmedRoutingKey, message);
    }
}
