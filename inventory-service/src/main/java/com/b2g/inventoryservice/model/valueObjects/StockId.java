package com.b2g.inventoryservice.model.valueObjects;


import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.UUID;
@Getter
@Embeddable
public class StockId {

    UUID bookId;
    UUID libraryId;


    public StockId(UUID bookId, UUID libraryId) {
        this.bookId = bookId;
        this.libraryId = libraryId;
    }
}
