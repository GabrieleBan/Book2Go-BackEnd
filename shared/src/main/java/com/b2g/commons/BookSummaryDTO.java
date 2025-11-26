package com.b2g.commons;

import lombok.Builder;

import java.util.List;
import java.util.Set;
import java.util.UUID;
@Builder
public record BookSummaryDTO(
        UUID id,
        String title,
        String authors,
        String publisher,
        String coverImageUrl,
        Set<CategoryDTO> categories
) {}
