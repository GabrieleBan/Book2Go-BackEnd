package com.b2g.inventoryservice.service;

import com.b2g.commons.LendingMessage;
import com.b2g.inventoryservice.model.LendingBooksReservations;
import com.b2g.inventoryservice.model.PhysicalBookIdentifier;
import com.b2g.inventoryservice.repository.ReservationsRepository;
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
    private final ReservationsRepository reservationsRepository;
    private final RabbitTemplate rabbitTemplate;
    public void reserveLendingBook(LendingMessage message) {
        Random rn = new Random();

        int i = rn.nextInt();
        reservationsRepository.save(LendingBooksReservations.builder()
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
