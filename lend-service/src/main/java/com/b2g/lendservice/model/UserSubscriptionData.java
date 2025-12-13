package com.b2g.lendservice.model;

import com.b2g.commons.SubscriptionType;
import lombok.Getter;

@Getter
public class UserSubscriptionData {

    private final SubscriptionType tier;
    private final int maxConcurrentLends;

    public UserSubscriptionData(SubscriptionType tier, int maxConcurrentLends) {
        this.tier = tier;
        this.maxConcurrentLends = maxConcurrentLends;
    }

    public boolean canAccess(SubscriptionType requiredTier) {
        return tier.isAtLeast(requiredTier);
    }

    public String getTierName() {
        return tier.name();
    }
}