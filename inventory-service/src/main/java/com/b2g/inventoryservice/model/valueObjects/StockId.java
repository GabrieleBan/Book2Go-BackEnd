package com.b2g.inventoryservice.model.valueObjects;


import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Getter
@Embeddable
@NoArgsConstructor
public class StockId {

    UUID bookId;
    UUID libraryId;


    public StockId(UUID bookId, UUID libraryId) {
        this.bookId = bookId;
        this.libraryId = libraryId;
    }
}
