package com.b2g.commons;

import java.util.List;

public enum LendState {
    PROCESSING,
    AWAITING,
    RESERVATION,
    LENDING,
    CONCLUDED,
    FAILED,
    LATE;

    public static List<LendState> activeStates() {
        return List.of(PROCESSING, RESERVATION,AWAITING, LENDING, LATE);
    }

    public boolean isActive() {
        return activeStates().contains(this);
    }
}