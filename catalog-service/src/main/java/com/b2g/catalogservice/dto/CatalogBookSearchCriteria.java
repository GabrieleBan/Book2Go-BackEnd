package com.b2g.catalogservice.dto;

import com.b2g.catalogservice.model.VO.FormatType;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record CatalogBookSearchCriteria(
        String title,
        String author,
        String publisher,
        Set<UUID> categoryIds,
        FormatType formatType,
        Integer minRating,

        BigDecimal minPrice,
        BigDecimal maxPrice
) {}