package com.b2g.lendservice.service.infrastructure;

import com.b2g.commons.ReservedBookMessage;
import com.b2g.lendservice.model.vo.LendableCopy;
import com.b2g.lendservice.service.application.LendingsApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class InventoryReservationListener {

    private final LendingsApplicationService lendingsApplicationService;
    @Value("${app.rabbitmq.bindingkey.book.reserved}")
    private String readyKey;

    @RabbitListener(queues = "${app.rabbitmq.queue.name}.inventory.arrivals")
    public void handleLendingEvent(
            ReservedBookMessage message,
            @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {

        log.info("Routing key ricevuta: {}", routingKey);
        log.info("Messaggio ricevuto: {}", message);

        LendableCopy copy= new LendableCopy(message.getBookId(), message.getCopyNumber());

        if (routingKey.equals(readyKey)) {

            lendingsApplicationService.assignCopyToLend(copy,message.getLibraryId());
        }
        else {
            log.warn("Routing key non gestita: {}", routingKey);
        }
    }
}
