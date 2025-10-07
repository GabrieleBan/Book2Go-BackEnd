package com.b2g.notificationservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Value("${spring.mail.host}")
    private String SMTP_HOST;
    @Value("${spring.mail.port}")
    private int SMTP_PORT;
    @Value("${spring.mail.username}")
    private String SMTP_USERNAME;
    @Value("${spring.mail.password}")
    private String SMTP_PASSWORD;
    @Value("${spring.mail.properties.mail.smtp.auth}")
    private String SMTP_AUTH;
    @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
    private String SMTP_STARTTLS;
    @Value("${spring.mail.properties.mail.debug}")
    private String SMTP_DEBUG;



    @Bean
    public JavaMailSender mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(SMTP_HOST);
        mailSender.setPort(SMTP_PORT);
        mailSender.setUsername(SMTP_USERNAME);
        mailSender.setPassword(SMTP_PASSWORD);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", SMTP_AUTH);
        props.put("mail.smtp.starttls.enable", SMTP_STARTTLS);
        props.put("mail.debug", SMTP_DEBUG);
        return mailSender;
    }
}
