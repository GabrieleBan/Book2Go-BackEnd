package com.b2g.lendservice.dto;

import com.b2g.commons.SubscriptionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class LendingOptionDTO {
    @Column(nullable = false)
    private String description="";
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

}