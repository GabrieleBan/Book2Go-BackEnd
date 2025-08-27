package com.b2g.catalogservice.dto;

import java.math.BigDecimal;
import java.util.List;

public record BookFormatCreateDTO(
        String formatType,
        BigDecimal purchasePrice,
        Integer stockQuantity, // Nullable, only for PHYSICAL
        boolean isAvailableForPurchase,
        boolean isAvailableForRental,
        boolean isAvailableOnSubscription,
        List<RentalOptionCreateDTO> rentalOptions
) {}
