package com.b2g.rentalservice.dto;

import com.b2g.commons.FormatType;
import com.b2g.rentalservice.model.RentalOption;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Builder
@Data
public class RentalFormatDto {
    private UUID formatId;
    @Enumerated(EnumType.STRING)
    private FormatType formatType;
    private Integer stockQuantity;
    @Column(nullable = false)
    private boolean isAvailableOnSubscription;
}
