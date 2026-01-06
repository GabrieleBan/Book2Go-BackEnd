package com.b2g.catalogservice.dto;

import com.b2g.catalogservice.model.Entities.BookFormat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record BookFormatDTO(
        UUID id,
        String formatType, // e.g., "EBOOK", "PHYSICAL"
        String isbn,
        BigDecimal purchasePrice
) {

}
