package com.b2g.rentalservice.service;

import com.b2g.commons.LendingMessage;
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
public class InventoryLendEventListener {

    private final RentalService rentalService;
    @Value("${app.rabbitmq.bindingkey.lend.ready}")
    private String readyKey;

    @RabbitListener(queues = "${app.rabbitmq.queue.name}.inventory.arrivals")
    public void handleLendingEvent(
            LendingMessage message,
            @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {

        log.info("Routing key ricevuta: {}", routingKey);
        log.info("Messaggio ricevuto: {}", message);

        if (routingKey.equals(readyKey)) {
            rentalService.markLendingAsWaiting(message);
        }
        else {
            log.warn("Routing key non gestita: {}", routingKey);
        }
    }
}
