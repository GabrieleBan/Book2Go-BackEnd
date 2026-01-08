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
    public boolean canBeNext(LendState next) {
        if (next == null) return false;

        return switch (this) {

            case PROCESSING ->
                    next == AWAITING
                            || next == LENDING
                            || next == FAILED;

            case AWAITING ->
                    next == LENDING
                            || next == FAILED;

            case LENDING ->
                    next == CONCLUDED
                            || next == LATE
                            || next == FAILED;

            case LATE ->
                    next == CONCLUDED
                            || next == FAILED;

            case RESERVATION ->
                    next == PROCESSING
                            || next == FAILED;

            case CONCLUDED, FAILED ->
                    false;
        };
    }
    public boolean canMoveFrom(LendState current) {
        if (current == null) return false;

        return switch (this) {

            case PROCESSING ->
                    current == RESERVATION;

            case AWAITING ->
                    current == PROCESSING;

            case LENDING ->
                    current == PROCESSING
                            || current == AWAITING;

            case CONCLUDED ->
                    current == LENDING
                            || current == LATE;

            case LATE ->
                    current == LENDING;

            case FAILED ->
                    current == PROCESSING
                            || current == AWAITING
                            || current == LENDING
                            || current == LATE
                            || current == RESERVATION;

            case RESERVATION ->
                    current == PROCESSING;
        };
    }
}