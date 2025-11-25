package com.b2g.readerservice.service;

import com.b2g.commons.UserRegistrationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationListener {
    @Value("${app.rabbitmq.service.prefix}")
    private String servicePrefix;
    private final ReaderService readerService;

    @RabbitListener(queues = "${app.rabbitmq.service.prefix}"+".user.queue")
    public void handleUserRegistration(UserRegistrationMessage message) {
        log.info("Ricevuto messaggio di conferma utente: {}", message);
        readerService.addReaderBasicInfo(message);

    }

}

