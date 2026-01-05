package com.b2g.catalogservice.service.infrastructure;

import com.b2g.catalogservice.model.Entities.CatalogBook;
import com.b2g.commons.CatalogBookCreatedEvent;
import com.b2g.commons.CategoryDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogEventPublisher {

    private final RabbitTemplate safeRabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.routingkey.book.creation}")
    private String routingKeyParentBookCreated;


    public void publishCatalogBookCreatedEvent(CatalogBook catalogBook) {
        CatalogBookCreatedEvent event = new CatalogBookCreatedEvent(
                catalogBook.getId(),
                catalogBook.getTitle(),
                catalogBook.getAuthor(),
                catalogBook.getPublisher(),
                catalogBook.getCategories().stream()
                        .map(c -> new CategoryDTO(c.getId(), c.getName()))
                        .collect(Collectors.toSet())
        );

        try {
            safeRabbitTemplate.invoke(ops -> {
                ops.execute(channel -> {
                    byte[] body = safeRabbitTemplate.getMessageConverter()
                            .toMessage(event, new MessageProperties())
                            .getBody();

                    channel.basicPublish(
                            exchangeName,
                            routingKeyParentBookCreated,
                            true, // mandatory
                            null,
                            body
                    );
                    return null;
                });
                ops.waitForConfirmsOrDie(3000); // attende conferma sincrona dal broker
                return null;
            });
            log.info("Evento CatalogBookCreated pubblicato: {}", catalogBook.getId());
        } catch (Exception e) {
            log.error("Errore nella pubblicazione dell'evento CatalogBookCreated", e);
            throw new RuntimeException(e);
        }
    }


}