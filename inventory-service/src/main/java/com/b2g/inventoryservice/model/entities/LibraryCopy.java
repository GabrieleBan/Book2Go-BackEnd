package com.b2g.inventoryservice.model.entities;

import com.b2g.inventoryservice.exceptions.AvailabilityException;
import com.b2g.inventoryservice.model.valueObjects.CopyCondition;
import com.b2g.inventoryservice.model.valueObjects.CopyId;
import com.b2g.inventoryservice.model.valueObjects.AvailabilityState;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.UUID;

@Entity
@Getter
public class LibraryCopy {

    @EmbeddedId
    private CopyId id;

    @Enumerated(EnumType.STRING)
    private AvailabilityState useState;

    @Enumerated(EnumType.STRING)
    private CopyCondition condition;

    protected LibraryCopy() {}

    private LibraryCopy(CopyId id) {
        this.id = id;
        this.useState = AvailabilityState.FREE;
        this.condition = CopyCondition.PERFECT;
    }

    public static LibraryCopy create(CopyId id) {
        return new LibraryCopy(id);
    }

    // =====================
    // DOMAIN BEHAVIOR
    // =====================

    public void reserve() {
        ensureUsable();
        if (useState != AvailabilityState.FREE) {
            throw new IllegalStateException("Copy is not free");
        }
        useState = AvailabilityState.RESERVED;
    }

    public void markInUse() {
        if (useState != AvailabilityState.RESERVED) {
            throw new IllegalStateException("Copy must be reserved before use");
        }
        useState = AvailabilityState.IN_USE;
    }

    public void markReturned() {
        if (useState != AvailabilityState.IN_USE) {
            throw new IllegalStateException("Copy is not in use");
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
            throw new IllegalStateException("Copy is not usable");
        }
    }
}