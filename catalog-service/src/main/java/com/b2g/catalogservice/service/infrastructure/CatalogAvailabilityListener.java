package com.b2g.catalogservice.service.infrastructure;
import com.b2g.catalogservice.service.application.BookFormatApplicationService;
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
public class CatalogAvailabilityListener {

    private final BookFormatApplicationService formatAppService;

    @Value("${app.rabbitmq.bindinkey.availability.available}")
    private String availableKey;

    @Value("${app.rabbitmq.bindinkey.availability.low}")
    private String lowKey;

    @Value("${app.rabbitmq.bindinkey.availability.out}")
    private String outKey;

    @Value("${app.rabbitmq.bindinkey.availability.not_available}")
    private String notAvailableKey;

    @RabbitListener(queues = "${app.rabbitmq.queue.name}.availability.queue")
    public void handleAvailabilityEvent(
            BookAvailabilityChangedEvent event,
            @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey
    ) {
        log.info(
            "Ricevuto availability event: bookId={}, routingKey={}",
            event.formatId,
            routingKey
        );
        try {


            if (routingKey.equals(availableKey)) {
                formatAppService.markBookAvailable(event.formatId);
            } else if (routingKey.equals(lowKey)) {
                formatAppService.markBookLowAvailability(event.formatId);
            } else if (routingKey.equals(outKey)) {
                formatAppService.markBookOutOfStock(event.formatId);
            } else if (routingKey.equals(notAvailableKey)) {
                formatAppService.markBookNotAvailable(event.formatId);
            } else {
                log.warn("Routing key non gestita: {}", routingKey);
            }
        }catch (Exception e) {log.error("Errore availability ricevuta per libro {}", event.formatId +" messaggio : "+ e.getMessage());}
    }
    public record BookAvailabilityChangedEvent(
            UUID formatId
    ) {}
}