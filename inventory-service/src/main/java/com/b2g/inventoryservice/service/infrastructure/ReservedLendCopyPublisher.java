package com.b2g.inventoryservice.service.infrastructure;

import com.b2g.commons.LendState;
import com.b2g.commons.ReservedBookMessage;
import com.b2g.inventoryservice.model.entities.LibraryCopy;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
@Slf4j
@Service
@RequiredArgsConstructor
public class ReservedLendCopyPublisher {
    private final RabbitTemplate safeRabbitTemplate;
    @Value("${app.rabbitmq.routingkey.lend.book.reserved}")
    private  String reservedLendCopykey;
    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;


    public void notifyCopyReserved(LibraryCopy copy) {
        ReservedBookMessage message = new ReservedBookMessage();
        message.setCopyNumber(copy.getId().getCopyNumber());
        message.setBookId(copy.getId().getBookId());
        message.setLibraryId(copy.getLibraryId());
        try {
            String finalRoutingKey = reservedLendCopykey;
            safeRabbitTemplate.invoke(ops -> {
                ops.execute(channel -> {

                    byte[] body = safeRabbitTemplate.getMessageConverter()
                            .toMessage(message, new MessageProperties())
                            .getBody();

                    // Pubblica il messaggio con mandatory=true → errori NO_ROUTE arrivano subito
                    channel.basicPublish(
                            exchangeName,
                            finalRoutingKey,
                            true,
                            null,
                            body
                    );
                    return null;
                });

                // Attende conferma sincrona dal broker
                ops.waitForConfirmsOrDie(3000);

                return null;
            });

        } catch (Exception e) {
            log.error("Errore nella consegna del messaggio: abort della creazione del lend", e);
            throw new RuntimeException(e);

        }
    }
}
