package com.b2g.inventoryservice.model.entities;

import com.b2g.inventoryservice.exceptions.ReservationException;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    @Embedded
    private CopyId copyId; // copia fisica assegnata



    private Instant reservedAt;

    @Enumerated(EnumType.STRING)
    private ReservationState state;

    private Reservation(CopyId copyId) {

        this.copyId = copyId;
        this.reservedAt = Instant.now();
        this.state = ReservationState.CONFIRMED;
    }

    public static Reservation create(CopyId copyId) {
        return new Reservation(copyId);
    }


    public void markInUse(LibraryCopy copy) {
        if (state != ReservationState.CONFIRMED) {
            throw new ReservationException("Reservation must be CONFIRMED to start use");
        }
        if(! copy.getId().equals(this.copyId)) {throw new ReservationException("Trying to retrieve a reserved copy that does not match the copy id of the used reservation");
        }
        state = ReservationState.IN_USE;
    }

    public void complete() {
        if (state != ReservationState.IN_USE) {
            throw new ReservationException("Reservation must be IN_USE to complete");
        }
        state = ReservationState.COMPLETED;
    }

    public void cancel() {
        if (state == ReservationState.IN_USE || state == ReservationState.COMPLETED) {
            throw new ReservationException("Cannot cancel ongoing or completed reservation");
        }
        state = ReservationState.CANCELLED;
    }
}