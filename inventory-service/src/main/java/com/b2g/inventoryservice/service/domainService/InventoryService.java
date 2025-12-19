package com.b2g.inventoryservice.service.domainService;

import com.b2g.inventoryservice.model.entities.LibraryCopy;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final LibraryCopyRepository copyRepository;

    /**
     * Marca una copia come in uso
     */
    @Transactional
    public void markCopyInUse(UUID libraryId, UUID bookId, int copyNumber) {
        LibraryCopy copy = copyRepository.findByLibraryIdAndBookIdAndCopyNumber(libraryId, bookId, copyNumber)
                .orElseThrow(() -> new IllegalStateException("Copy not found"));

        copy.markInUse();
        copyRepository.save(copy);
    }

    /**
     * Marca una copia come restituita (disponibile)
     */
    @Transactional
    public void markCopyReturned(UUID libraryId, UUID bookId, int copyNumber) {
        LibraryCopy copy = copyRepository.findByLibraryIdAndBookIdAndCopyNumber(libraryId, bookId, copyNumber)
                .orElseThrow(() -> new IllegalStateException("Copy not found"));

        copy.markReturned();
        copyRepository.save(copy);
    }

    /**
     * Segnala copia come non disponibile (danneggiata o fuori servizio)
     */
    @Transactional
    public void markCopyUnavailable(UUID libraryId, UUID bookId, int copyNumber) {
        LibraryCopy copy = copyRepository.findByLibraryIdAndBookIdAndCopyNumber(libraryId, bookId, copyNumber)
                .orElseThrow(() -> new IllegalStateException("Copy not found"));

        copy.markUnavailable();
        copyRepository.save(copy);
    }
}