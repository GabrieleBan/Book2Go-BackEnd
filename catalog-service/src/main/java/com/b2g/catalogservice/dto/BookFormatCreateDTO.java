package com.b2g.catalogservice.dto;

import com.b2g.commons.RentalOptionCreateDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record BookFormatCreateDTO(
        @NotBlank(message = "Format type cannot be blank")
        String formatType,

        @NotNull(message = "Purchase price cannot be null")
        @DecimalMin(value = "0.0", inclusive = false, message = "Purchase price must be greater than 0")
        BigDecimal purchasePrice,

        @Min(value = 0, message = "Stock quantity cannot be negative")
        Integer stockQuantity, // Nullable, only for PHYSICAL

        boolean isAvailableForPurchase,
        boolean isAvailableForRental,
        boolean isAvailableOnSubscription

//        @Valid
//        List<RentalOptionCreateDTO> rentalOptions
) {}
