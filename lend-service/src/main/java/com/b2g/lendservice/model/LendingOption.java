package com.b2g.lendservice.model;

import com.b2g.commons.SubscriptionType;
import com.b2g.lendservice.dto.LendingOptionDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
    @NotNull
    @Column(nullable = false)
    private int durationDays=30;
    @NotNull
    @Column(nullable = false)
    private Integer maxRenewals;
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionType minRequiredTier;

    public boolean isApplicableFor(UserSubscriptionData userSubscription) {
        return userSubscription.canAccess(minRequiredTier);
    }
    public static LendingOption create(LendingOptionDTO option) {
        LendingOption newOption= new LendingOption();
        newOption.setDescription(option.getDescription());
        newOption.setDurationDays(option.getDurationDays());
        newOption.setMaxRenewals(option.getMaxRenewals());
        newOption.setMinRequiredTier(option.getMinRequiredTier());
        return newOption;

    }
    @JsonIgnore
    public SubscriptionType getMinRequiredTierName() {
        return minRequiredTier;
    }
}