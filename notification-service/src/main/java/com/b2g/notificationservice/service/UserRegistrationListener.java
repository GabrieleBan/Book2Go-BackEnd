package com.b2g.notificationservice.service;

import com.b2g.notificationservice.dto.UserRegistrationMessage;
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

    @Value("${app.confirmation.base-url}")
    private String confirmationBaseUrl;

    @RabbitListener(queues = "${app.rabbitmq.queue.user-signup}")
    public void handleUserRegistration(UserRegistrationMessage message) {
        log.info("Ricevuto messaggio di registrazione utente: {}", message);
        sendConfirmationEmail(message);
    }

    private void sendConfirmationEmail(UserRegistrationMessage message) {
        String subject = "Conferma la tua registrazione";
        String confirmationLink = confirmationBaseUrl + "/auth/confirm/" + message.getUuid();
        String text = String.format("Ciao %s,\n\nGrazie per la registrazione! Conferma la tua email cliccando sul link: %s\n\nSe non hai richiesto la registrazione, ignora questa email.",
                message.getUsername(), confirmationLink);

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(message.getEmail());
        mailMessage.setSubject(subject);
        mailMessage.setText(text);

        try {
            mailSender.send(mailMessage);
            log.info("Email di conferma inviata a {}", message.getEmail());
        } catch (Exception e) {
            log.error("Errore nell'invio della email di conferma a {}: {}", message.getEmail(), e.getMessage());
        }
    }
}

