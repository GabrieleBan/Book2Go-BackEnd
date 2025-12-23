package com.b2g.inventoryservice.service.applicationService;

import com.b2g.inventoryservice.exceptions.*;
import com.b2g.inventoryservice.model.entities.LibraryCopy;
import com.b2g.inventoryservice.model.entities.Reservation;
import com.b2g.inventoryservice.model.entities.ReservationRequest;

import com.b2g.inventoryservice.model.valueObjects.AvailabilityState;
import com.b2g.inventoryservice.model.valueObjects.ReservationState;
import com.b2g.inventoryservice.repository.LibraryCopyRepository;
import com.b2g.inventoryservice.repository.ReservationRepository;
import com.b2g.inventoryservice.repository.ReservationRequestRepository;
import com.b2g.inventoryservice.service.domainService.InventoryService;
import com.b2g.inventoryservice.service.domainService.ReservationService;
import com.b2g.inventoryservice.service.infrastructure.ReservedLendCopyPublisher;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryApplicationService {

    private final ReservationRequestRepository requestRepository;
    private final LibraryCopyRepository copyRepository;
    private final ReservationRepository reservationRepository;

    private final InventoryService inventoryService;
    private final ReservationService reservationService;
    private final ReservedLendCopyPublisher reservedLendCopyPublisher;

    @Transactional
    public Reservation assignReservation(ReservationRequest request) {

        LibraryCopy copy =copyRepository.findFirstByUseStateAndLibraryIdAndId_BookId(
                AvailabilityState.FREE,
                request.getLibraryId(),
                request.getBookId());
        if (copy == null) {
            throw new LibraryCopyException("Library copy not found");
        }
        Reservation reservation = reservationService.assignCopy(request,copy);
        requestRepository.delete(request);
        reservationRepository.save(reservation);
        copyRepository.save(copy);
        reservedLendCopyPublisher.notifyCopyReserved(copy);
        return reservation;
    }
    @Transactional
    public LibraryCopy retrieveCopy(UUID libraryId, UUID bookId, Integer copyNumber) {
        LibraryCopy copy= copyRepository.findByLibraryIdAndId_BookIdAndId_CopyNumber(libraryId, bookId, copyNumber);
        if (copy == null) {
            throw new LibraryCopyException("Library copy not found");
        }
        Reservation reservation= reservationRepository.findByCopyIdAndState(copy.getId(), ReservationState.CONFIRMED);
        if (reservation == null) {
            throw new ReservationException("No reservation found for this copy ");
        }

        copy= reservationService.retrieveReservedCopy(reservation,copy);

        reservationRepository.save(reservation);
        return copyRepository.save(copy);

    }


    public void createReservationRequest(UUID libraryId, UUID bookId) {
        LibraryCopy copy= copyRepository.findFirstByUseStateAndLibraryIdAndId_BookId(AvailabilityState.FREE, libraryId, bookId);
        ReservationRequest request= reservationService.createRequest(libraryId, bookId);

        request=requestRepository.save(request);
        if(copy != null) {
            try {
                assignReservation(request);
            }catch (Exception e) {log.error("Error assigning reservation request"+ e.getMessage());}
        }
    }


}