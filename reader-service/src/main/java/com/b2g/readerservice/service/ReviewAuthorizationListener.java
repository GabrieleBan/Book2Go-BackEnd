package com.b2g.readerservice.service;

import com.b2g.commons.ReviewConfirmationDTO;
import com.b2g.commons.UserRegistrationMessage;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewAuthorizationListener {

    @Value("${app.rabbitmq.service.prefix}")
    private String servicePrefix;
    private final ReaderService readerService;

    @RabbitListener(queues = "${app.rabbitmq.service.prefix}"+".review.authorization.queue")
    public void handleReviewAuthorizationRequest(ReviewConfirmationDTO message) {
        log.info("Ricevuta richiesta di autorizzazione: {}", message);
        readerService.checkUserReviewAuthorization(message);
    }
}
