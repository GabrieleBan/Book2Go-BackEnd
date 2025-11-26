package com.b2g.catalogservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
@Builder
public record BookCreateRequestDTO(
        @NotBlank(message = "Title cannot be blank")
        @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
        String title,

        @NotBlank(message = "Author cannot be blank")
        @Size(min = 1, max = 255, message = "Author must be between 1 and 255 characters")
        String author,

        @NotBlank(message = "ISBN cannot be blank")
        @Size(min = 10, max = 17, message = "ISBN must be between 10 and 17 characters")
        String isbn,

        @Size(max = 2000, message = "Description cannot exceed 2000 characters")
        String description,

        @Size(max = 255, message = "Publisher cannot exceed 255 characters")
        String publisher,

        @NotNull(message = "Publication date cannot be null")
        LocalDate publicationDate,

        @NotEmpty(message = "At least one category must be specified")
        Set<UUID> categoryIds,

        @NotEmpty(message = "At least one format must be specified")
        @Valid
        List<BookFormatCreateDTO> formats
) {}
