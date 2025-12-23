package com.b2g.inventoryservice.model.valueObjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.UUID;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor
public class CopyId {



    @Column(name = "book_id", nullable = false)
    private UUID bookId;

    @Column(name = "copy_number", nullable = false)
    private Integer copyNumber;
    public CopyId( UUID formatId,int copyNumber) {
        this.bookId = formatId;
        this.copyNumber = copyNumber;
    }
}