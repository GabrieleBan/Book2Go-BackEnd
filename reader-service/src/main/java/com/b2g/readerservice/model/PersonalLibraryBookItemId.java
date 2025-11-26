package com.b2g.readerservice.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data
public class PersonalLibraryBookItemId implements Serializable {
    private UUID archetypeBookId;
    private UUID formatId;
}
