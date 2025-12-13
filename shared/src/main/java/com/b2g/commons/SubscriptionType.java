package com.b2g.commons;

import lombok.Getter;

@Getter
public enum SubscriptionType {
    UNSUBSCRIBED(0),
    SUB_TIER1(1),
    SUB_TIER2(2);

    private final int level;

    SubscriptionType(int level) {
        this.level = level;
    }

    public boolean isAtLeast(SubscriptionType other) {
        return this.level >= other.level;
    }

}