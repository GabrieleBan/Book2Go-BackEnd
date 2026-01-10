package com.b2g.inventoryservice.repository;

import com.b2g.inventoryservice.model.entities.LibraryCopy;
import com.b2g.inventoryservice.model.valueObjects.AvailabilityState;
import com.b2g.inventoryservice.model.valueObjects.CopyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LibraryCopyRepository extends JpaRepository<LibraryCopy, CopyId> {


    @Query("""
           SELECT COALESCE(MAX(c.id.copyNumber), 0)
           FROM LibraryCopy c
           WHERE c.id.bookId = :bookId
           """)
    Integer findMaxCopyNumberByBookId(@Param("bookId") UUID bookId);

    LibraryCopy findByLibraryIdAndId_BookIdAndId_CopyNumberAndUseState(UUID libraryId, UUID bookId, Integer copyNumber, AvailabilityState availabilityState);

    LibraryCopy findByLibraryIdAndId_BookIdAndId_CopyNumber(UUID idLibraryId, UUID idBookId, Integer idCopyNumber);

    LibraryCopy findFirstByUseStateAndLibraryIdAndId_BookId(AvailabilityState availabilityState, UUID libraryId, UUID bookId);
}
