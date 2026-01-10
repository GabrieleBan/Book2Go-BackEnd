package com.b2g.inventoryservice.model.entities;

import com.b2g.inventoryservice.exceptions.AvailabilityException;
import com.b2g.inventoryservice.model.valueObjects.CopyCondition;
import com.b2g.inventoryservice.model.valueObjects.CopyId;
import com.b2g.inventoryservice.model.valueObjects.AvailabilityState;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@Entity
@Getter
@EqualsAndHashCode
public class LibraryCopy {

    @EmbeddedId
    private CopyId id;
    @Column(name = "library_id", nullable = false)
    private UUID libraryId;

    @Enumerated(EnumType.STRING)
    private AvailabilityState useState;

    @Enumerated(EnumType.STRING)
    private CopyCondition condition;

    protected LibraryCopy() {}

    private LibraryCopy(CopyId id,UUID libraryId,CopyCondition condition) {
        this.id = id;
        this.useState = AvailabilityState.FREE;
        this.condition = condition;
        this.libraryId = libraryId;
    }

    public static LibraryCopy create(CopyId id, UUID libraryId,CopyCondition condition) {
        return new LibraryCopy(id, libraryId,condition);
    }

    // =====================
    // DOMAIN BEHAVIOR
    // =====================

    public void reserve() {
        ensureUsable();
        if (useState != AvailabilityState.FREE) {
            throw new AvailabilityException("Copy is not free");
        }
        useState = AvailabilityState.RESERVED;
    }

    public void markInUse() {
        if (useState != AvailabilityState.RESERVED) {
            throw new AvailabilityException("Copy must be reserved before use");
        }
        useState = AvailabilityState.IN_USE;
    }

    public void markReturned() {
        if (useState != AvailabilityState.IN_USE) {
            throw new AvailabilityException("Copy is not in use");
        }
        useState = AvailabilityState.FREE;
    }

    public void markUnavailable() {
        useState = AvailabilityState.UNAVAILABLE;
    }

    public void changeCondition(CopyCondition newCondition) {
        this.condition = newCondition;
        if (!newCondition.isUsable()) {
            markUnavailable();
        }
    }

    private void ensureUsable() {
        if (!condition.isUsable()) {
            throw new AvailabilityException("Copy is not usable");
        }
    }
}