package com.b2g.inventoryservice.service.infrastructure;

import com.b2g.commons.LendingMessage;
import com.b2g.inventoryservice.service.applicationService.InventoryApplicationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class lendReservationsListener {
    @Value("${app.rabbitmq.bindingkey.lend.request.created}")
    private String requestLendReservationKey;
    private final RabbitTemplate rabbitTemplate;
    private final InventoryApplicationService inventoryService;

    @RabbitListener(queues = "inventory.lend.queue")
    public void handleLendingEvent(
            LendingMessage message,
            @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {

        log.info("Routing key ricevuta: {}", routingKey);
        log.info("Messaggio ricevuto: {}", message);

        if (routingKey.equals(requestLendReservationKey)) {
            inventoryService.createReservationRequest(message.getLibraryId(),message.getFormatId());
        }
        else {
            log.warn("Routing key non gestita: {}", routingKey);
        }
    }
}


