package com.b2g.readerservice.service;


import com.b2g.commons.LendingMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LendListener {
    @Value("${app.rabbitmq.service.prefix}")
    private String servicePrefix;
    private final ReaderService readerService;
    @Value("${app.rabbitmq.binding-key.lend.created}")
    private String lendCreatedBindingKey;
    @Value("${app.rabbitmq.binding-key.lend.ended}")
    private String lendEndedBindingKey;



    @RabbitListener(queues = "${app.rabbitmq.service.prefix}"+".lend.queue")
    public void handleLendMessage(
            LendingMessage message,
            @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {

        log.info("Routing key ricevuta: {}", routingKey);
        log.info("Messaggio ricevuto: {}", message);

       if (routingKey.equals(lendCreatedBindingKey)) {
           readerService.addLendToLibrary(message);
       }
       else if (routingKey.equals(lendEndedBindingKey)) {
           readerService.markLendAsExpired(message);
       }
    }
}
