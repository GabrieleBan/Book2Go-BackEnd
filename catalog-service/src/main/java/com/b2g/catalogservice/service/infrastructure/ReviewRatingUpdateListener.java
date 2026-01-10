package com.b2g.catalogservice.service.infrastructure;
import com.b2g.catalogservice.service.application.BookFormatApplicationService;
import com.b2g.catalogservice.service.application.CatalogBookApplicationService;
import com.b2g.commons.BookRatingUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewRatingUpdateListener {

    private final CatalogBookApplicationService catalogAppService;

    @Value("${app.rabbitmq.bindinkey.rating.updated}")
    private String ratingUpdatedBindingKey;

    @RabbitListener(queues = "${app.rabbitmq.queue.name}.rating.queue")
    public void handleAvailabilityEvent(
            BookRatingUpdateEvent event,
            @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey
    ) {
        log.info(
            "Ricevuto rating event: {}, routingKey={}",
            event,
            routingKey
        );
        try {
            if (routingKey.equals(ratingUpdatedBindingKey)) {
                catalogAppService.updateRating(event);
            }
        }catch (Exception e) {log.error("Errore Rating ricevuta per libro {}", event.bookId +" messaggio : "+ e.getMessage());}
    }

}