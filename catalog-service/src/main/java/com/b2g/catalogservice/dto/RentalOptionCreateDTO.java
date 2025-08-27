package com.b2g.catalogservice.dto;

import java.math.BigDecimal;

public record RentalOptionCreateDTO(int durationDays, BigDecimal price, String description) {}
