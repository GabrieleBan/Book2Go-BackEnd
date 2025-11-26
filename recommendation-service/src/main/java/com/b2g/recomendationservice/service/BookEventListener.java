package com.b2g.recomendationservice.service;

import com.b2g.commons.BookSummaryDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;




@Service
@RequiredArgsConstructor
@Slf4j

public class BookEventListener {
    @Value("${app.rabbitmq.service.prefix}")
    private String servicePrefix;
    private final RecommendationService recommendationService;

    @RabbitListener(queues = "${app.rabbitmq.service.prefix}"+".book.queue")
    public void handleBookCreation(BookSummaryDTO message) {
        log.info("Ricevuto messaggio di creazione Libro: {}", message);
        log.info("Creato un nuovo libro{}",recommendationService.addBookNode(message));

    }

}
