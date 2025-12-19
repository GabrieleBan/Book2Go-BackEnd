package com.b2g.inventoryservice.model.entities;

import com.b2g.inventoryservice.model.valueObjects.ReservationRequestState;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class ReservationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    private UUID libraryId;
    private UUID bookId; // formato/libro richiesto
    private UUID userId;

    private Instant requestedAt;

    @Enumerated(EnumType.STRING)
    private ReservationRequestState state;

    private Long assignedReservationId; // opzionale, null finché non viene assegnata una copia

    private ReservationRequest(UUID libraryId, UUID bookId, UUID userId) {
        this.libraryId = libraryId;
        this.bookId = bookId;
        this.userId = userId;
        this.requestedAt = Instant.now();
        this.state = ReservationRequestState.REQUESTED;
    }

    public static ReservationRequest create(UUID libraryId, UUID bookId, UUID userId) {
        return new ReservationRequest(libraryId, bookId, userId);
    }

    // =====================
    // DOMAIN METHODS
    // =====================
    public void reject() {
        if (state != ReservationRequestState.REQUESTED) {
            throw new IllegalStateException("Request cannot be rejected");
        }
        state = ReservationRequestState.REJECTED;
    }

    public void expire() {
        if (state != ReservationRequestState.REQUESTED) {
            throw new IllegalStateException("Only requested requests can expire");
        }
        state = ReservationRequestState.EXPIRED;
    }

    public void markAssigned(Long reservationId) {
        if (state != ReservationRequestState.REQUESTED) {
            throw new IllegalStateException("Only requested requests can be assigned");
        }
        this.assignedReservationId = reservationId;
        this.state = ReservationRequestState.ASSIGNED;
    }
}