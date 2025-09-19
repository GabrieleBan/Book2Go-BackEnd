package com.b2g.catalogservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record RentalOptionCreateDTO(
        @Min(value = 1, message = "Duration must be at least 1 day")
        int durationDays,

        @NotNull(message = "Price cannot be null")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        BigDecimal price,

        @Size(max = 200, message = "Description cannot exceed 200 characters")
        String description
) {}
