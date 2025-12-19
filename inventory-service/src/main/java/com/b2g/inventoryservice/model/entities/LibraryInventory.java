package com.b2g.inventoryservice.model.entities;


import com.b2g.inventoryservice.exceptions.StockException;
import com.b2g.inventoryservice.exceptions.StockQuantityException;
import com.b2g.inventoryservice.model.valueObjects.AvailabilityState;
import com.b2g.inventoryservice.model.valueObjects.CopyCondition;
import com.b2g.inventoryservice.model.valueObjects.StockId;
import com.b2g.inventoryservice.model.valueObjects.CopyId;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.*;

@Entity
@Table(name = "library_inventory")
@Getter
public class LibraryInventory {

    @Id
    @Column(name = "library_id", nullable = false, updatable = false)
    private UUID libraryId;

    // Relazione con RetailStock come Entity separata
    @OneToMany(
            mappedBy = "id.libraryId",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<RetailStock> retailStocks = new ArrayList<>();

    // LibraryCopy come Entity interna
    @OneToMany(
            mappedBy = "libraryInventory",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<LibraryCopy> copies = new ArrayList<>();

    protected LibraryInventory() {}

    public LibraryInventory(UUID libraryId) {
        this.libraryId = libraryId;
    }


    public void addRetailStock(UUID bookId, int quantity) {
        Optional<RetailStock> existing = retailStocks.stream()
                .filter(rs -> rs.getId().getBookId().equals(bookId))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().increase(quantity);
        } else {
            RetailStock stock = new RetailStock(new StockId(bookId, libraryId), quantity);
            retailStocks.add(stock);
        }
    }

    public void sellRetailCopy(UUID bookId) {
        RetailStock stock = retailStocks.stream()
                .filter(rs -> rs.getId().getBookId().equals(bookId))
                .findFirst()
                .orElseThrow(() -> new StockException("No retail stock for book: " + bookId));

        if (stock.getQuantity() <= 0)
            throw new StockQuantityException("No quantity left for book: " + bookId);

        stock.decrease();
    }

    public boolean hasRetailStock(UUID bookId) {
        return retailStocks.stream()
                .anyMatch(rs -> rs.getId().getBookId().equals(bookId) && rs.getQuantity() > 0);
    }

    // ======= LibraryCopy operations =======
    public void addCopy(UUID formatId, int copyNumber, CopyCondition condition, AvailabilityState availabilityState) {
        CopyId copyId = new CopyId(formatId, copyNumber);
        LibraryCopy copy = new LibraryCopy(this, copyId, condition);
        copies.add(copy);
    }

    public boolean hasAvailableCopy(UUID formatId) {
        return copies.stream()
                .anyMatch(c -> c.getId().getFormatId().equals(formatId) &&
                        c.getUsageState() == CopyUsageState.FREE);
    }

    public LibraryCopy reserveCopy(UUID formatId) {
        LibraryCopy copy = copies.stream()
                .filter(c -> c.getId().getFormatId().equals(formatId) &&
                        c.getUsageState() == CopyUsageState.FREE)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No available copy for book: " + formatId));
        copy.markReserved();
        return copy;
    }

    public List<LibraryCopy> getCopies() {
        return Collections.unmodifiableList(copies);
    }

    public List<RetailStock> getRetailStocks() {
        return Collections.unmodifiableList(retailStocks);
    }
}