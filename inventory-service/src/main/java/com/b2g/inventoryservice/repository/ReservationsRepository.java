package com.b2g.inventoryservice.repository;

import com.b2g.inventoryservice.model.LendingBooksReservations;
import com.b2g.inventoryservice.model.PhysicalBookIdentifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReservationsRepository extends JpaRepository<LendingBooksReservations,Long> {



//    LendingBooksReservations findByLibraryIDAndPhysicalBookIdentifier_FormatId(UUID libraryID, UUID physicalBookIdentifierFormatId);
//
//    LendingBooksReservations findByLibraryIDAndPhysicalBookIdentifier_FormatId(UUID libraryID, UUID physicalBookIdentifierFormatId, Pageable pageable);

    LendingBooksReservations findByLibraryIDAndPhysicalBookIdentifier(UUID libraryID, PhysicalBookIdentifier physicalBookIdentifier);
}
