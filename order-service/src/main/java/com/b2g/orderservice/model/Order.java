package com.b2g.orderservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data // Lombok
public class Order {
    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItem> items;

    @Embedded
    private ShippingAddress shippingAddress;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status; // Stato visibile all'utente (PENDING, COMPLETED, SHIPPED, FAILED)

    // Campo CRUCIALE per la SAGA
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SagaStatus sagaStatus; // Stato interno della SAGA (AWAITING_PAYMENT, AWAITING_LIBRARY, etc.)

    private String failureReason; // Per debug in caso di fallimento della SAGA

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
