package com.b2g.commons;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
@Builder
public record BookSummaryDTO(
        UUID id,
        String title,
        String author,
        String publisher,
        String coverImageUrl,
        Map<String, BigDecimal> prices,
        float rating,
        Set<CategoryDTO> categories
) {}
