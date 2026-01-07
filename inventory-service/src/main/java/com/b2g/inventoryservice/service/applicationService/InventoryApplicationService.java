package com.b2g.inventoryservice.service.applicationService;

import com.b2g.inventoryservice.exceptions.*;
import com.b2g.inventoryservice.model.entities.*;

import com.b2g.inventoryservice.model.valueObjects.AvailabilityState;
import com.b2g.inventoryservice.model.valueObjects.ReservationState;
import com.b2g.inventoryservice.model.valueObjects.StockId;
import com.b2g.inventoryservice.repository.*;
import com.b2g.inventoryservice.service.domainService.InventoryService;
import com.b2g.inventoryservice.service.domainService.ReservationService;
import com.b2g.inventoryservice.service.infrastructure.CatalogBookFormatListener;
import com.b2g.inventoryservice.service.infrastructure.InventoryAvailabilityEventPublisher;
import com.b2g.inventoryservice.service.infrastructure.ReservedLendCopyPublisher;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryApplicationService {

    private final ReservationRequestRepository requestRepository;
    private final LibraryCopyRepository copyRepository;
    private final ReservationRepository reservationRepository;
    private final ReferenceBookRepository referenceBookRepository;
    private final InventoryService inventoryService;
    private final ReservationService reservationService;
    private final ReservedLendCopyPublisher reservedLendCopyPublisher;
    private final StockRepository stockRepository;
    private final InventoryAvailabilityEventPublisher inventoryAvailabilityEventPublisher;
    private final LibraryRepository libraryRepository;
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

    @Transactional
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

    @Transactional
    public void initializeBookInInventory(CatalogBookFormatListener.PhysicalReferenceBook bookToAdd) {
        if(!bookToAdd.isPhisical) {
            log.info("Libro digitale, non ho bisogno di inizializzare stock");
            return;
        }
        ReferenceBook referenceBook= new ReferenceBook(bookToAdd.formatId, bookToAdd.formatType);
        referenceBookRepository.save(referenceBook);
        // al momento non inizializzo con stock a 0 in tutte le librerie
    }

    @Transactional
    public RetailStock changeStockQuantity(UUID bookId, UUID libraryId, int quantity) {
        StockId id = new StockId(bookId, libraryId);
        RetailStock stock = stockRepository.findById(id)
                .orElse(null);
        if(stock == null) {
            stock = initializeStockInLibrary(bookId, libraryId, quantity);
        }
        else {
            inventoryService.changeStockQuantity(stock, quantity);
        }
        stockRepository.save(stock);
        recalculateAvailability(bookId);
        return stock;
    }

    private RetailStock initializeStockInLibrary(UUID bookId, UUID libraryId, int quantity) {
        RetailStock stock;
        ReferenceBook book=referenceBookRepository.findById(bookId).orElse(null);
        Library lib= libraryRepository.findById(libraryId).orElse(null);
        if(lib == null) {
            throw new BookShopNotFoundException("Id di questa libreria non corrisponde a nessuna");
        }
        stock=inventoryService.initLibraryStock(book,lib, quantity);
        return stock;
    }


    public boolean isStockAvailable(UUID bookFormatId, UUID libraryId) {
        StockId id = new StockId(bookFormatId, libraryId);
        return stockRepository.findById(id)
                .map(inventoryService::isAvailable)
                .orElse(false);
    }

    private void recalculateAvailability(UUID bookId) {
        ReferenceBook book= referenceBookRepository.findById(bookId).orElse(null);
        if(book == null) {throw new StockException("Libro non esiste nell'inventario");   }
        List<RetailStock> stocks= stockRepository.findById_BookId(bookId);

        if ( inventoryService.changeBookAvailability(book, stocks)) {
            referenceBookRepository.save(book);
            inventoryAvailabilityEventPublisher.publishAvailabilityChanged(book);
        }
    }

    public Set<RetailStock> getAllStocksForBook(UUID bookId) {
        if(!referenceBookRepository.existsById(bookId)) {
            throw new StockException("Libro non gestito dall' inventario");
        }
        return new HashSet<>(stockRepository.findById_BookId(bookId));
    }

    public RetailStock getStock(UUID libraryId, UUID bookId) {
        if(!referenceBookRepository.existsById(bookId)) {
            throw new StockException("Libro non gestito dall' inventario");
        }
        StockId id = new StockId(bookId, libraryId);
        RetailStock stock = stockRepository.findById(id).orElse(null);
        if(stock == null) {
            throw new StockQuantityException("Libro non ancora o non più gestito in questa libreria");
        }
        return stock;
    }
}