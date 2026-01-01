package com.b2g.catalogservice.dto;

import com.b2g.commons.RentalOptionCreateDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

public record BookFormatCreateDTO(
        @NotBlank(message = "Format type cannot be blank")
        String formatType,

        @NotNull(message = "Purchase price cannot be null")
        @DecimalMin(value = "0.0", inclusive = false, message = "Purchase price must be greater than 0")
        BigDecimal purchasePrice,
        @Max(value = 100, message = "discount cannot be more then 100%")
        @Min(value = 0 , message = "discount cannot be less then 0%")
        Short discountPercentage,


        @NotBlank(message = "ISBN cannot be blank")
        @Size(min = 10, max = 17, message = "ISBN must be between 10 and 17 characters")
        String isbn,
        @Min(value = 0, message = "Stock quantity cannot be negative")
        Integer stockQuantity ,// Nullable, only for PHYSICAL
        Integer numberOfPages //l'ho appena aggiunto anche in format,

) {}
