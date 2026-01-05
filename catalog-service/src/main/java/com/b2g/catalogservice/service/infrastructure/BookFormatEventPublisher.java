package com.b2g.catalogservice.service.infrastructure;
import com.b2g.catalogservice.model.Entities.BookFormat;
import com.b2g.commons.BookFormatCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookFormatEventPublisher {

    private final RabbitTemplate safeRabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.routingkey.format.created}")
    private String routingKeyFormatCreated;

    public void publishBookFormatCreatedEvent(BookFormat bookFormat) {
        BookFormatCreatedEvent event = new BookFormatCreatedEvent(
                bookFormat.getId(),
                bookFormat.getBookId(),
                bookFormat.getFormatType().name(),
                bookFormat.isPhysical()
        );

        try {
            safeRabbitTemplate.invoke(ops -> {
                ops.execute(channel -> {
                    byte[] body = safeRabbitTemplate.getMessageConverter()
                            .toMessage(event, new MessageProperties())
                            .getBody();

                    channel.basicPublish(
                            exchangeName,
                            routingKeyFormatCreated,
                            true, // mandatory
                            null,
                            body
                    );
                    return null;
                });
                ops.waitForConfirmsOrDie(3000); // conferma sincrona
                return null;
            });
            log.info("Evento BookFormatCreated pubblicato: {}", bookFormat.getId());
        } catch (Exception e) {
            log.error("Errore nella pubblicazione dell'evento BookFormatCreated", e);
            throw new RuntimeException(e);
        }
    }


}