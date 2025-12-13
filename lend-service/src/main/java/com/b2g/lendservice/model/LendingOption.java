package com.b2g.lendservice.model;

import com.b2g.commons.SubscriptionType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;


@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "lending_options")
public class LendingOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private int durationDays=30;

    @Column(nullable = false)
    private Integer maxRenewals;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionType minRequiredTier;

    public boolean isApplicableFor(UserSubscriptionData userSubscription) {
        return userSubscription.canAccess(minRequiredTier);
    }

    public SubscriptionType getMinRequiredTierName() {
        return minRequiredTier;
    }
}