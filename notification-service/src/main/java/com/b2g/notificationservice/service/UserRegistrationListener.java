package com.b2g.notificationservice.service;

import com.b2g.commons.UserRegistrationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationListener {

    private final JavaMailSender mailSender;
    private final NotificationService notificationService;

    @Value("${app.confirmation.base-url}")
    private String confirmationBaseUrl;

    @RabbitListener(queues = "${app.rabbitmq.queue.user-signup}")
    public void handleUserRegistration(UserRegistrationMessage message) {
        log.info("Ricevuto messaggio di registrazione utente: {}", message);
        notificationService.sendConfirmationEmail(message);
    }


}

