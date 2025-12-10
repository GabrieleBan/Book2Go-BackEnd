package com.b2g.libraryservice.service;

import com.b2g.commons.LendingMessage;
import com.b2g.libraryservice.model.LendingBooksReservations;
import com.b2g.libraryservice.model.PhysicalBookIdentifier;
import com.b2g.libraryservice.repository.LendingBooksRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class InventoryService {
    @Value("${app.rabbitmq.exchange}")
    private String exchange;
    private final LendingBooksRepository lendingBooksRepository;
    private final RabbitTemplate rabbitTemplate;
    public void reserveLendingBook(LendingMessage message) {
        Random rn = new Random();

        int i = rn.nextInt();
        lendingBooksRepository.save(LendingBooksReservations.builder()
                        .physicalBookIdentifier(new PhysicalBookIdentifier(i,message.getFormatId()))
                        .requestedAt(LocalDate.now())
                        .reservedAt(LocalDate.now())
                        .libraryID(message.getLibraryId())
                        .build());
        message.setPhysicalId(i);
        rabbitTemplate.convertAndSend(
                exchange,             // default exchange
                "lend.ready",   // routing key
                message         // corpo del messaggio
        );



    }
}
