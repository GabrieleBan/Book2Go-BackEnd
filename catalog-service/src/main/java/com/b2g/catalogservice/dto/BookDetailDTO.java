package com.b2g.catalogservice.dto;

import com.b2g.commons.CategoryDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;



public record BookDetailDTO(
        UUID id,
        String title,
        String author,
        String description,
        String publisher,
        LocalDate publicationDate,
        List<CategoryDTO> categories,
        List<BookFormatDTO> availableFormats
) {}