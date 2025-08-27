package com.b2g.catalogservice.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record BookCreateRequestDTO(
        String title,
        String author,
        String isbn,
        String description,
        String publisher,
        LocalDate publicationDate,
        Set<UUID> categoryIds,
        List<BookFormatCreateDTO> formats
) {}
