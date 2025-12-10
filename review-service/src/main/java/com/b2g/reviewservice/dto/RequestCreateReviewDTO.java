package com.b2g.reviewservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RequestCreateReviewDTO(


        @NotNull(message = "BookId cannot be null")
        UUID bookId,
        float overallScore,
        @NotBlank(message = "Review cannot be blank")
        @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
        String title,
        @NotBlank(message = "Review cannot be blank")
        @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
        String text
) {}
