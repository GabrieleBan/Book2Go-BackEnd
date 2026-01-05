package com.b2g.inventoryservice.service.infrastructure;

import com.b2g.commons.BookFormatCreatedEvent;
import com.b2g.inventoryservice.service.applicationService.InventoryApplicationService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Component
public class CatalogBookFormatListener {

    @Value("${app.rabbit.bindingkey.book.format.created}")
    private String bookFormatCreatedKey;

    private final InventoryApplicationService inventoryService;


    @RabbitListener(queues = "inventory.catalog.queue")
    public void handleBookFormatCreated(
            BookFormatCreatedEvent message,
            @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {

        log.info("Routing key ricevuta: {}", routingKey);
        log.info("Messaggio ricevuto: {}", message);

        if (!routingKey.equals(bookFormatCreatedKey)) {
            log.warn("Routing key non gestita: {}", routingKey);
            return;
        }

        PhysicalReferenceBook bookToAdd= new PhysicalReferenceBook(message.formatId, message.formatType, message.isPhisical);
        inventoryService.initializeBookInInventory(bookToAdd);

        log.info("Stock inizializzato per formato {} del libro {}", message.getFormatId(), message.getBookId());
    }
    @AllArgsConstructor
    public static class PhysicalReferenceBook {
        public UUID formatId;
        public String formatType;
        public boolean isPhisical;
    }

}