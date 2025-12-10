package com.b2g.catalogservice.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record BookFormatDTO(
        UUID id,
        String formatType, // e.g., "EBOOK", "PHYSICAL"
        BigDecimal purchasePrice,
        boolean isAvailableForPurchase,
        boolean isAvailableForRental,
        boolean isAvailableOnSubscription
//        List<RentalOptionDTO> rentalOptions
) {}
