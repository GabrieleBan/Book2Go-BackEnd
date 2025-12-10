package com.b2g.orderservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;


@Entity
@Table(name = "order_items")
@Data
public class OrderItem {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    private UUID bookFormatId; // Riferimento al formato specifico acquistato/noleggiato
    private String bookTitle;  // Denormalizzato per convenienza
    private int quantity;
    private BigDecimal unitPrice;
    @Enumerated(EnumType.STRING)
    private OrderItemType type; // PURCHASE o RENTAL
    private Integer rentalDurationDays; // Se RENTAL
}
