package com.b2g.inventoryservice.repository;

import com.b2g.inventoryservice.model.entities.LibraryCopy;
import com.b2g.inventoryservice.model.valueObjects.AvailabilityState;
import com.b2g.inventoryservice.model.valueObjects.CopyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LibraryCopyRepository extends JpaRepository<LibraryCopy, CopyId> {



    LibraryCopy findByLibraryIdAndId_BookIdAndId_CopyNumberAndUseState(UUID libraryId, UUID bookId, Integer copyNumber, AvailabilityState availabilityState);

    LibraryCopy findByLibraryIdAndId_BookIdAndId_CopyNumber(UUID idLibraryId, UUID idBookId, Integer idCopyNumber);

    LibraryCopy findFirstByUseStateAndLibraryIdAndId_BookId(AvailabilityState availabilityState, UUID libraryId, UUID bookId);
}
