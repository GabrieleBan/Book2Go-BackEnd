package com.b2g.notificationservice.service;

import com.b2g.commons.LendingMessage;
import com.b2g.commons.UserRegistrationMessage;
import com.b2g.notificationservice.model.UserNotificationBillboard;
import com.b2g.notificationservice.repository.BillboardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private final JavaMailSender mailSender;
    private final BillboardRepository billboardRepository;
    @Value("${app.confirmation.base-url}")
    private String confirmationBaseUrl;

    protected void sendConfirmationEmail(UserRegistrationMessage message) {
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
            sendGreetings(message.getUsername(),message.getUuid());


        } catch (Exception e) {
            log.error("Errore nell'invio della email di conferma a {}: {}", message.getEmail(), e.getMessage());
        }
    }

    private void sendGreetings(String username, UUID uuid) {
        UserNotificationBillboard newNotification = UserNotificationBillboard.builder()
                .title("Benvenuto "+username+" ")
                .description("Ora non ti resta che conferma la tua registrazione")
                .timestamp(LocalDateTime.now())
                .userid(uuid)
                .build();
        billboardRepository.save(newNotification);
    }

    public Page<UserNotificationBillboard> getMyBillboard(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return billboardRepository.findByUserid(userId, pageable);

    }
    private String buildBookInfo(LendingMessage message) {
        return "Libro ID: " + message.getBookId() + ", Formato: " + message.getFormatType();
    }
    public void createLendCreatedNotification(LendingMessage message) {
        UserNotificationBillboard note = UserNotificationBillboard.builder()
                .userid(message.getUserId())
                .title("Prestito creato")
                .description("Il prestito per " + buildBookInfo(message) + " è stato creato con successo.")
                .timestamp(LocalDateTime.now())
                .build();

        billboardRepository.save(note);
    }

    public void createLendFailedNotification(LendingMessage message) {
        UserNotificationBillboard note = UserNotificationBillboard.builder()
                .userid(message.getUserId())
                .title("Prestito fallito")
                .description("Il prestito per " + buildBookInfo(message) + " non è andato a buon fine.")
                .timestamp(LocalDateTime.now())
                .build();

        billboardRepository.save(note);
    }

    public void createProcessingNotification(LendingMessage message) {
        UserNotificationBillboard note = UserNotificationBillboard.builder()
                .userid(message.getUserId())
                .title("Prestito in elaborazione")
                .description("Il prestito per " + buildBookInfo(message) + " è in fase di elaborazione")
                .timestamp(LocalDateTime.now())
                .build();

        billboardRepository.save(note);
    }

    public void createLendEndedNotification(LendingMessage message) {
        UserNotificationBillboard note = UserNotificationBillboard.builder()
                .userid(message.getUserId())
                .title("Prestito terminato")
                .description("Il prestito per " + buildBookInfo(message) + " è terminato.")
                .timestamp(LocalDateTime.now())
                .build();

        billboardRepository.save(note);
    }

    public void createLendArrivededNotification(LendingMessage message) {
        UserNotificationBillboard note = UserNotificationBillboard.builder()
                .userid(message.getUserId())
                .title("Prestito pronto")
                .description("Il libro " + buildBookInfo(message) + " è pronto per essere ritirato in libreria "+ buildLibraryInfo(message.getLibraryId()))
                .timestamp(LocalDateTime.now())
                .build();

        billboardRepository.save(note);
    }

    private String buildLibraryInfo(UUID libraryId) {
        return "Libro ID: " + libraryId;
    }
}