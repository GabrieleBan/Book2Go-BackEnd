package com.b2g.recomendationservice.dto;

import java.util.UUID;

public record ReviewDTO(
        UUID readerId,
        UUID bookId,
        float rating
) {}