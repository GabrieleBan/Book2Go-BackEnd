package com.b2g.inventoryservice.model.entities;

import com.b2g.inventoryservice.model.valueObjects.StockAvailabilityStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Entity
@Getter
public class ReferenceBook {

    @Id
    @Column(name = "book_id",unique = true, nullable = false)
    private UUID bookId;
    private String formatType;
    @Enumerated(EnumType.STRING)
    private StockAvailabilityStatus stockAvailabilityStatus;



    protected ReferenceBook() {}

    public ReferenceBook(UUID bookId, String formatType) {
        this.bookId = bookId;
        this.formatType = formatType;
        this.stockAvailabilityStatus = StockAvailabilityStatus.NOT_AVAILABLE;
    }


    private boolean updateAvailability(StockAvailabilityStatus newStatus) {
        if (this.stockAvailabilityStatus != newStatus) {
            this.stockAvailabilityStatus = newStatus;
            return true;
        }
        return false;
    }

    public boolean updateAvailabilityBasedOnTotalStock(Integer total) {
        StockAvailabilityStatus newStatus = StockAvailabilityStatus.fromTotalQuantity(total);
        return this.updateAvailability(newStatus);
    }
}