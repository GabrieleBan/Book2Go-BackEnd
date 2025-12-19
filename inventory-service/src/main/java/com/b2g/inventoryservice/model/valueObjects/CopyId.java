package com.b2g.inventoryservice.model.valueObjects;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.UUID;

@Embeddable
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CopyId {

    private UUID libraryId;
    private UUID formatId;
    private Integer copyNumber;
    public CopyId(UUID libraryId, UUID formatId) {
        this.libraryId = libraryId;
        this.formatId = formatId;
        this.copyNumber = null;
    }
}