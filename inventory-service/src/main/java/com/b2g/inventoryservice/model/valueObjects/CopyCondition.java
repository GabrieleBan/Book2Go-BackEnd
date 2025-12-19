package com.b2g.inventoryservice.model.valueObjects;

public enum CopyCondition {
    PERFECT(true),
    SLIGHTLY_DAMAGED(true),
    DAMAGED(true),
    HEAVILY_DAMAGED(false),
    DESTROYED(false);

    private final boolean usable;

    CopyCondition(boolean usable) {
        this.usable = usable;
    }

    public boolean isUsable() {
        return usable;
    }
}