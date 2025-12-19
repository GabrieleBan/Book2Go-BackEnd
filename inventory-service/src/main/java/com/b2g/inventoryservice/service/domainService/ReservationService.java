package com.b2g.inventoryservice.service.domainService;

import com.b2g.inventoryservice.model.entities.ReservationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRequestRepository requestRepository;
    private final ReservationRepository reservationRepository;
    private final LibraryCopyRepository copyRepository;


    public ReservationRequest createRequest(UUID libraryId, UUID bookId, UUID userId) {
        ReservationRequest request = ReservationRequest.create(libraryId, bookId, userId);
        return requestRepository.save(request);
    }


    @Transactional
    public Reservation assignCopy(Long requestId, Integer copyNumber) {
        ReservationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        if (request.getState() != ReservationRequestState.REQUESTED) {
            throw new IllegalStateException("Request is not in REQUESTED state");
        }

        LibraryCopy copy = copyRepository.findByLibraryIdAndBookIdAndCopyNumberAndUseState(
                        request.getLibraryId(),
                        request.getBookId(),
                        copyNumber,
                        CopyUseState.FREE)
                .orElseThrow(() -> new IllegalStateException("Copy not available"));

        // Aggiorna stato della copia fisica
        copy.reserve();
        copyRepository.save(copy);

        // Crea prenotazione concreta
        Reservation reservation = Reservation.create(copy.getId(), request.getBookId(), request.getUserId(), request.getLibraryId());
        reservationRepository.save(reservation);

        // Aggiorna request come assegnata
        request.markAssigned(reservation.getReservationId());
        requestRepository.save(request);

        return reservation;
    }
}