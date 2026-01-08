package com.b2g.inventoryservice.service.domainService;

import com.b2g.inventoryservice.model.entities.LibraryCopy;
import com.b2g.inventoryservice.model.entities.Reservation;
import com.b2g.inventoryservice.model.entities.ReservationRequest;
import com.b2g.inventoryservice.model.valueObjects.AvailabilityState;
import com.b2g.inventoryservice.model.valueObjects.ReservationRequestState;
import com.b2g.inventoryservice.repository.LibraryCopyRepository;
import com.b2g.inventoryservice.repository.ReservationRepository;
import com.b2g.inventoryservice.repository.ReservationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
@Service
@RequiredArgsConstructor
public class ReservationService {

    public ReservationRequest createRequest(UUID libraryId, UUID bookId) {
        ReservationRequest request = ReservationRequest.create(libraryId, bookId);
        return request;
    }


    @Transactional
    public Reservation assignCopy(ReservationRequest request, LibraryCopy copy) {
//        if (request.getState() != ReservationRequestState.REQUESTED) {
//            throw new IllegalStateException("Request is not in REQUESTED state");
//        }

        if (copy == null) {
            throw new IllegalStateException("Copy not found");
        }

        Reservation reservation = request.assignTo(copy);
        copy.reserve();
        request.markAssigned();

        return reservation;
    }

    public LibraryCopy retrieveReservedCopy(Reservation reservation, LibraryCopy copy) {

        copy.markInUse();
        reservation.markInUse(copy);

        return copy;
    }
}