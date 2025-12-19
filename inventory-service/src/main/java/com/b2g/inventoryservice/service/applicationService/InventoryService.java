package com.b2g.inventoryservice.service.applicationService;

import com.b2g.commons.LendingMessage;
import com.b2g.inventoryservice.model.entities.ReservationRequest;
import com.b2g.inventoryservice.model.valueObjects.CopyId;
import com.b2g.inventoryservice.repository.ReservationsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Random;
import java.util.UUID;

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
        reservationsRepository.save(  ReservationRequest.create(message.getLibraryId(), message.getFormatId(), message.getUserId()));
        message.setPhysicalId(i);
        rabbitTemplate.convertAndSend(
                exchange,             // default exchange
                "lend.ready",   // routing key
                message         // corpo del messaggio
        );



    }
}
