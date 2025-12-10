package com.b2g.catalogservice.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;
@Builder
public record RentalOptionDTO(UUID id, int durationDays, BigDecimal price, String description) {}

