package com.b2g.lendservice.model;

import jakarta.persistence.Embeddable;
import lombok.*;
import net.minidev.json.annotate.JsonIgnore;

import java.util.UUID;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class LendableCopy {
    UUID lendableBookId; // riferimento all’entità LendableBook
    Integer copyNumber;  // null se digitale/audiobook
    @JsonIgnore
    public boolean isPhysical() {
        return copyNumber != null;
    }

    public void setCopyNumber(Integer copyNumber) throws Exception {
        if (copyNumber != null) {throw new Exception("Libro fisico già assegnato");}
        this.copyNumber = copyNumber;
    }
}