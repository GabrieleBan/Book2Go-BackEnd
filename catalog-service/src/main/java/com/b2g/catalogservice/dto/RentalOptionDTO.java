package com.b2g.catalogservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record RentalOptionDTO(UUID id, int durationDays, BigDecimal price, String description) {}

