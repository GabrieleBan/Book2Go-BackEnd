package com.b2g.commons;

import lombok.Builder;

import java.util.Set;
import java.util.UUID;
@Builder
public record BookSummaryMessage(
        UUID id,
        String title,
        String author,
        String publisher,
        Set<CategoryDTO> categories
) {}
