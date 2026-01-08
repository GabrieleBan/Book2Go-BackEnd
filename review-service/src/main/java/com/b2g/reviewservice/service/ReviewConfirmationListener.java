package com.b2g.reviewservice.service;


import com.b2g.commons.ReaderBookPossessionResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewConfirmationListener {
    private final ReviewService reviewService;
    @Value("${app.rabbitmq.service.prefix}")
    private String servicePrefix;

    @Value("${app.rabbitmq.binding-key.review.authorized}")
    private String reviewAuthorizedBindingKey;

    @Value("${app.rabbitmq.binding-key.review.rejected}")
    private String reviewRejectedBindingKey;


    @RabbitListener(queues = "${app.rabbitmq.service.prefix}" + ".authorization.queue")
    public void handleReviewEvents(ReaderBookPossessionResultDTO confirmedReview,
                                   @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {

        log.info("Routing key usata: {}", routingKey);
        log.info("Messaggio ricevuto: {}", confirmedReview);

        if (routingKey.equals(reviewAuthorizedBindingKey) || routingKey.equals(reviewRejectedBindingKey)) {
            log.info("Ricevuto messaggio di conferma review: {}", confirmedReview);
            reviewService.handleReaderPossessionResult(confirmedReview);
        } else {
            log.warn("Routing key non gestita: {}", routingKey);
        }
    }
}
