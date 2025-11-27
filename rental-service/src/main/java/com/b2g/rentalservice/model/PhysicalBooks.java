package com.b2g.rentalservice.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PhysicalBooks {
    @EmbeddedId
    private PhysicalBookIdentifier id;

}
