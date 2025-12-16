package com.b2g.lendservice.service.infrastructure;

import com.b2g.commons.LendState;
import com.b2g.commons.LendingMessage;
import com.b2g.lendservice.model.LendableBook;
import com.b2g.lendservice.model.Lending;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LendEventPublisher {
    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;
    @Value("${app.rabbitmq.routingkey.lend.request.created}")
    private String routingKeyLendRequestCreated;
    @Value("${app.rabbitmq.routingkey.lend.created}")
    private String routingKeyLendCreated;
    private final RabbitTemplate safeRabbitTemplate;


    /**
     * Pubblicazione evento Lending (appena creato e fisico -> Processing , se digitale o assegnato in libreria -> Lending )
     */
    public void publishLendingEventAsync(Lending lending, LendableBook lendableBook) {
        LendingMessage message = LendingMessage.builder()
                .lendId(lending.getId())
                .bookId(lendableBook.getBookId())
                .formatId(lending.getCopy().getLendableBookId())
                .physicalId(lending.getCopy().getCopyNumber())
                .userId(lending.getUserId())
                .lendState(lending.getState())
                .libraryId(lending.getLibraryId())
                .startDate(lending.getPeriod().getStart())
                .endDate(lending.getPeriod().getEnd())
                .build();
        String routingKey = "";
        if (lending.getState().equals(LendState.LENDING))
            routingKey = routingKeyLendCreated;
        if (lending.getState().equals(LendState.PROCESSING))
            if (lending.getLibraryId() != null)
                routingKey = routingKeyLendRequestCreated;
        log.info("Used key: " + routingKey);
        if (routingKey.isEmpty()) {
            log.error("routing key is empty, unexpected lend state is " + lending.getState());
            throw new NotImplementedException("Routing key is empty");
        }
        try {
            String finalRoutingKey = routingKey;
            safeRabbitTemplate.invoke(ops -> {
                ops.execute(channel -> {
                    // Converte il DTO in byte[]
                    byte[] body = safeRabbitTemplate.getMessageConverter()
                            .toMessage(message, new MessageProperties())
                            .getBody();

                    // Pubblica il messaggio con mandatory=true → errori NO_ROUTE arrivano subito
                    channel.basicPublish(
                            exchangeName,
                            finalRoutingKey,
                            true,  // mandatory
                            null,  // MessageProperties opzionale, il converter gestisce headers
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
