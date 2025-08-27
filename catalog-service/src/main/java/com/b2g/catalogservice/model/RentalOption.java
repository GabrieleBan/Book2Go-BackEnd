package com.b2g.catalogservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "rental_options")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Link back to the format this option applies to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_format_id", nullable = false)
    private BookFormat bookFormat;

    @Column(nullable = false)
    private Integer durationDays; // e.g., 7, 14, 30

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // You could add a description like "Standard Rental", "Extended Access"
    private String description;
}
