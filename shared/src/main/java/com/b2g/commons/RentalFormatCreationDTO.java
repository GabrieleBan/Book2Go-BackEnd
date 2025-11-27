package com.b2g.commons;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record RentalFormatCreationDTO (
    @NotBlank(message = "Format type cannot be blank")
    String formatType,
    UUID formatId,
    UUID parentBookId,

    @NotNull(message = "Purchase price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Purchase price must be greater than 0")
    BigDecimal purchasePrice,

    @Min(value = 0, message = "Stock quantity cannot be negative")
    Integer stockQuantity, // Nullable, only for PHYSICAL

    boolean isAvailableForRental,
    boolean isAvailableOnSubscription
){}
