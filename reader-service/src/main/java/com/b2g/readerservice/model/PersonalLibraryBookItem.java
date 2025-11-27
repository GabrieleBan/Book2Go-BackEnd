package com.b2g.readerservice.model;


import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalLibraryBookItem {
    @EmbeddedId
    private PersonalLibraryBookItemId id;
    private UUID userId;
    private Date expirationDate;
    @Enumerated(EnumType.STRING)
    private BookOwnershipState state;
}
