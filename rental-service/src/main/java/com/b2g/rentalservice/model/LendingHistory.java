package com.b2g.rentalservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Data
public class LendingHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private UUID formatId;
    private int physBookId;
    @ManyToOne
    @JoinColumn(name = "rental_option_id")
    private RentalOption rentalOption;
    private LocalDate startDate;
    private LocalDate endDate;
    private LendState state;
}
