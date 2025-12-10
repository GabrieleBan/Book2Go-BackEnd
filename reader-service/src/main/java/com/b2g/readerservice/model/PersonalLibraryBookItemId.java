package com.b2g.readerservice.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PersonalLibraryBookItemId implements Serializable {
    private UUID archetypeBookId;
    private UUID formatId;
    private UUID userId;
}
