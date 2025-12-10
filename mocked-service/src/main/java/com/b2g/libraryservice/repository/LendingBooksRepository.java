package com.b2g.libraryservice.repository;

import com.b2g.libraryservice.model.LendingBooksReservations;
import com.b2g.libraryservice.model.PhysicalBookIdentifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LendingBooksRepository extends JpaRepository<LendingBooksReservations,Long> {



//    LendingBooksReservations findByLibraryIDAndPhysicalBookIdentifier_FormatId(UUID libraryID, UUID physicalBookIdentifierFormatId);
//
//    LendingBooksReservations findByLibraryIDAndPhysicalBookIdentifier_FormatId(UUID libraryID, UUID physicalBookIdentifierFormatId, Pageable pageable);

    LendingBooksReservations findByLibraryIDAndPhysicalBookIdentifier(UUID libraryID, PhysicalBookIdentifier physicalBookIdentifier);
}
