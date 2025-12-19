package com.b2g.inventoryservice.repository;

import com.b2g.inventoryservice.model.entities.ReservationRequest;
import com.b2g.inventoryservice.model.valueObjects.CopyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReservationsRepository extends JpaRepository<ReservationRequest,Long> {



//    LendingBooksReservations findByLibraryIDAndPhysicalBookIdentifier_FormatId(UUID libraryID, UUID physicalBookIdentifierFormatId);
//
//    LendingBooksReservations findByLibraryIDAndPhysicalBookIdentifier_FormatId(UUID libraryID, UUID physicalBookIdentifierFormatId, Pageable pageable);

    ReservationRequest findByLibraryIDAndPhysicalBookIdentifier(UUID libraryID, CopyId physicalBookIdentifier);
}
