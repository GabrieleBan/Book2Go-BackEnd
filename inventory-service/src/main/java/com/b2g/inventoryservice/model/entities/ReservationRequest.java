package com.b2g.inventoryservice.model.entities;


import com.b2g.inventoryservice.exceptions.ReservationRequestException;
import com.b2g.inventoryservice.exceptions.ReservationRequestStateException;
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


    private Instant requestedAt;

    @Enumerated(EnumType.STRING)
    private ReservationRequestState state;

    private ReservationRequest(UUID libraryId, UUID bookId) {
        this.libraryId = libraryId;
        this.bookId = bookId;

        this.requestedAt = Instant.now();
        this.state = ReservationRequestState.REQUESTED;
    }

    public static ReservationRequest create(UUID libraryId, UUID bookId) {
        return new ReservationRequest(libraryId, bookId);
    }


    public void reject() {
        if (state != ReservationRequestState.REQUESTED) {
            throw new ReservationRequestStateException("Request cannot be rejected");
        }
        state = ReservationRequestState.REJECTED;
    }

    public void expire() {
        if (state != ReservationRequestState.REQUESTED) {
            throw new ReservationRequestStateException("Only requested requests can expire");
        }
        state = ReservationRequestState.EXPIRED;
    }

    public void markAssigned() {
        if (state == ReservationRequestState.ASSIGNED) {
            return;}
        if (state != ReservationRequestState.REQUESTED) {
            throw new ReservationRequestStateException("Only requested requests can be assigned");
        }
        this.state = ReservationRequestState.ASSIGNED;
    }

    public Reservation assignTo(LibraryCopy copy) {
        if (state != ReservationRequestState.REQUESTED) {
            throw new ReservationRequestStateException("Request not assignable");
        }

        if (!copy.getId().getBookId().equals(bookId)) {
            throw new ReservationRequestStateException("Copy does not match requested book");
        }



        this.state = ReservationRequestState.ASSIGNED;

        return Reservation.create(copy.getId());
    }
}