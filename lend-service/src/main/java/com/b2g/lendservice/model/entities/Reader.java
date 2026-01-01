package com.b2g.lendservice.model.entities;

import jakarta.persistence.Id;
import lombok.Getter;


import java.util.List;
import java.util.UUID;

@Getter
public class Reader {

    private final UUID readerId;
    private final List<Lending> activeLendings;

    public Reader(UUID readerId, List<Lending> activeLendings) {
        this.readerId = readerId;
        this.activeLendings = activeLendings;
    }

    public boolean canBorrowMore(int maxAllowed) {
        return activeLendings.size() < maxAllowed;
    }

    public boolean hasActiveLendingFor(UUID lendableBookId) {
        return activeLendings.stream()
                .anyMatch(l ->
                        l.getCopy()
                                .getLendableBookId()
                                .equals(lendableBookId)
                );
    }
}