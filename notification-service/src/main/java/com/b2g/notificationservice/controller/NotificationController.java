package com.b2g.notificationservice.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;



@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final JavaMailSender mailSender;

    @GetMapping({"", "/"})
    public ResponseEntity<?> helloWorld() {
        return ResponseEntity.ok("hello world from notification service");
    }

    @GetMapping("/send")
    public ResponseEntity<?> sendEmail(
            @RequestParam(defaultValue = "test@emanuelespadaro.com") String to,
            @RequestParam(defaultValue = "Benvenuto su BookToGo!") String subject
    ) {
        String htmlContent = """
            <html>
            <body style='font-family: Arial, sans-serif;'>
                <h2 style='color: #2d6a4f;'>BookToGo</h2>
                <p>Ciao!</p>
                <p>Benvenuto sulla piattaforma <b>BookToGo</b>, dove puoi <b>comprare</b> o <b>noleggiare</b> libri fisici e digitali!</p>
                <ul>
                    <li>Libri fisici nuovi e usati</li>
                    <li>eBook e audiolibri</li>
                    <li>Servizi di noleggio per studenti e appassionati</li>
                </ul>
                <p>Scopri le offerte e inizia subito a leggere!</p>
                <a href='https://booktogo.example.com' style='background: #2d6a4f; color: #fff; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>Visita BookToGo</a>
                <br><br>
                <small>Questa è una email di test inviata dal servizio di notifiche.</small>
            </body>
            </html>
        """;
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            return ResponseEntity.ok("Email inviata con successo a " + to);
        } catch (MessagingException e) {
            return ResponseEntity.status(500).body("Errore nell'invio della mail: " + e.getMessage());
        }
    }
}