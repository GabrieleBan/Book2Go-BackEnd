package com.b2g.inventoryservice.model.entities;

import com.b2g.inventoryservice.model.valueObjects.CopyId;
import com.b2g.inventoryservice.model.valueObjects.ReservationState;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue
    private UUID reservationId;

    @Embedded
    private CopyId copyId; // copia fisica assegnata

    private UUID bookId;
    private UUID userId;
    private UUID libraryId;

    private Instant reservedAt;

    @Enumerated(EnumType.STRING)
    private ReservationState state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", insertable = false, updatable = false)
    private Book book;

    private Reservation(CopyId copyId, UUID bookId, UUID userId, UUID libraryId) {
        this.reservationId = UUID.randomUUID();
        this.copyId = copyId;
        this.bookId = bookId;
        this.userId = userId;
        this.libraryId = libraryId;
        this.reservedAt = Instant.now();
        this.state = ReservationState.CONFIRMED;
    }

    public static Reservation create(CopyId copyId, UUID bookId, UUID userId, UUID libraryId) {
        return new Reservation(copyId, bookId, userId, libraryId);
    }

    // =====================
    // DOMAIN METHODS
    // =====================
    public void markInUse() {
        if (state != ReservationState.CONFIRMED) {
            throw new IllegalStateException("Reservation must be CONFIRMED to start use");
        }
        state = ReservationState.IN_USE;
    }

    public void complete() {
        if (state != ReservationState.IN_USE) {
            throw new IllegalStateException("Reservation must be IN_USE to complete");
        }
        state = ReservationState.COMPLETED;
    }

    public void cancel() {
        if (state == ReservationState.IN_USE || state == ReservationState.COMPLETED) {
            throw new IllegalStateException("Cannot cancel ongoing or completed reservation");
        }
        state = ReservationState.CANCELLED;
    }
}