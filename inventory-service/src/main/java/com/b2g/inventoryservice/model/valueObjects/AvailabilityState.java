package com.b2g.inventoryservice.model.valueObjects;


public enum AvailabilityState {
    FREE {
        public boolean canBeReserved() { return true; }
    },
    RESERVED,
    IN_USE,
    UNAVAILABLE;

    public boolean canBeReserved() { return false; }
}