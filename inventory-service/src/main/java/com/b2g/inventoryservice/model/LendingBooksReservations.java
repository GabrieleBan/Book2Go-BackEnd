package com.b2g.inventoryservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LendingBooksReservations {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;
    @Embedded
    private PhysicalBookIdentifier physicalBookIdentifier;
    private UUID libraryID;
    private LocalDate requestedAt;
    private LocalDate reservedAt;

}
