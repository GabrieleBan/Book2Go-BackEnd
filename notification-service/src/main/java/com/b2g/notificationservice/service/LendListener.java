package com.b2g.notificationservice.service;

import com.b2g.commons.LendingMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class LendListener {

    private final NotificationService notificationService;

    @Value("${app.rabbitmq.service.prefix}")
    private String servicePrefix;

    @Value("${app.rabbitmq.binding-key.lend.ready}")
    private String lendReadyForRetrieval;

    @Value("${app.rabbitmq.binding-key.lend.created}")
    private String lendCreatedBindingKey;

    @Value("${app.rabbitmq.binding-key.lend.failed}")
    private String lendFailedBindingKey;

    @Value("${app.rabbitmq.binding-key.lend.ended}")
    private String lendEndedBindingKey;

    @Value("${app.rabbitmq.binding-key.lend.request.created}")
    private String lendRequestCreated;

    @RabbitListener(queues = "${app.rabbitmq.queue.name.suffix}.lend.queue")
    public void handleLendingEvent(
            LendingMessage message,
            @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {

        log.info("Routing key ricevuta: {}", routingKey);
        log.info("Messaggio ricevuto: {}", message);

        if (routingKey.equals(lendCreatedBindingKey)) {
            notificationService.createLendCreatedNotification(message);
        }
        else if (routingKey.equals(lendFailedBindingKey)) {
            notificationService.createLendFailedNotification(message);
        }
        else if (routingKey.equals(lendRequestCreated)) {
            notificationService.createProcessingNotification(message);
        }
        else if (routingKey.equals(lendEndedBindingKey)) {
            notificationService.createLendEndedNotification(message);
        }
        else if (routingKey.equals(lendReadyForRetrieval)) {
            notificationService.createLendArrivededNotification(message);
        }
        else {
            log.warn("Routing key non gestita: {}", routingKey);
        }
    }
}