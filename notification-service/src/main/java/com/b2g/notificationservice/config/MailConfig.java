package com.b2g.notificationservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;
@Configuration
@EnableConfigurationProperties(MailProperties.class)
public class MailConfig {

    private final MailProperties mailProperties;

    public MailConfig(MailProperties mailProperties) {
        this.mailProperties = mailProperties;
    }

    @Bean
    public JavaMailSender mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailProperties.getHost());
        mailSender.setPort(mailProperties.getPort());
        mailSender.setUsername(mailProperties.getUsername());
        mailSender.setPassword(mailProperties.getPassword());
        mailSender.setDefaultEncoding(mailProperties.getDefaultEncoding().name());

        Properties props = mailSender.getJavaMailProperties();
        props.putAll(mailProperties.getProperties());

        props.putIfAbsent("mail.transport.protocol", "smtp");
        props.putIfAbsent("mail.smtp.auth", mailProperties.getProperties().getOrDefault("mail.smtp.auth", "true"));
        props.putIfAbsent("mail.smtp.starttls.enable", mailProperties.getProperties().getOrDefault("mail.smtp.starttls.enable", "true"));
        props.putIfAbsent("mail.debug", mailProperties.getProperties().getOrDefault("mail.debug", "false"));

        return mailSender;
    }
}