package com.b2g.inventoryservice.service.infrastructure;

import com.b2g.inventoryservice.model.entities.ReferenceBook;
import com.b2g.inventoryservice.model.valueObjects.StockAvailabilityStatus;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryAvailabilityEventPublisher {

    private final RabbitTemplate safeRabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.routingkey.availability.available}")
    private String availableKey;

    @Value("${app.rabbitmq.routingkey.availability.low}")
    private String lowStockKey;

    @Value("${app.rabbitmq.routingkey.availability.out}")
    private String outOfStockKey;

    @Value("${app.rabbitmq.routingkey.availability.not_available}")
    private String notAvailableKey;

    public void publishAvailabilityChanged(ReferenceBook book) {
        String routingKey = resolveRoutingKey(book.getStockAvailabilityStatus());

        BookAvailabilityChangedEvent event =
                new BookAvailabilityChangedEvent(book.getBookId());

        try {
            safeRabbitTemplate.convertAndSend(
                    exchangeName,
                    routingKey,
                    event
            );

            log.info(
                "Availability event published: bookId={}, status={}",
                book.getBookId(),
                book.getStockAvailabilityStatus()
            );

        } catch (Exception e) {
            log.error("Failed to publish availability event", e);
            throw new RuntimeException(e);
        }
    }

    private String resolveRoutingKey(StockAvailabilityStatus status) {
        return switch (status) {
            case AVAILABLE      -> availableKey;
            case LOW_STOCK      -> lowStockKey;
            case OUT_OF_STOCK   -> outOfStockKey;
            case NOT_AVAILABLE  -> notAvailableKey;
        };
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    private static class BookAvailabilityChangedEvent{
        UUID formatId;
    }
}
