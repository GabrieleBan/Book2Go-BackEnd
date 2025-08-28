package com.b2g.catalogservice.controller;

import com.b2g.catalogservice.dto.*;
import com.b2g.catalogservice.model.*;
import com.b2g.catalogservice.repository.BookRepository;
import com.b2g.catalogservice.repository.CategoryRepository;
import com.b2g.catalogservice.repository.BookFormatRepository;
import com.b2g.catalogservice.repository.RentalOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final BookFormatRepository bookFormatRepository;
    private final RentalOptionRepository rentalOptionRepository;

    @GetMapping({"", "/"})
    public ResponseEntity<?> getAllBooks(
            @RequestParam(required = false) Set<UUID> categoryIds,
            Pageable pageable) {
        return ResponseEntity.ok(Collections.emptyList());
    }

    @PostMapping({"", "/"})
    public ResponseEntity<BookDetailDTO> createBook(@RequestBody /* @Valid */ BookCreateRequestDTO request) {
        // Fetch categories if provided
        Set<Category> categories = new HashSet<>();
        if (request.categoryIds() != null && !request.categoryIds().isEmpty()) {
            categories = new HashSet<>(categoryRepository.findAllById(request.categoryIds()));
        }

        // Create Book entity from request
        Book book = Book.builder()
                .title(request.title())
                .author(request.author())
                .isbn(request.isbn())
                .description(request.description())
                .publisher(request.publisher())
                .publicationDate(request.publicationDate())
                .categories(categories)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .availableFormats(new ArrayList<>())
                .build();

        // Save the book first to get the ID
        Book savedBook = bookRepository.save(book);

        // Create and save book formats if provided
        List<BookFormat> bookFormats = new ArrayList<>();
        if (request.formats() != null && !request.formats().isEmpty()) {
            for (BookFormatCreateDTO formatDto : request.formats()) {
                // Create BookFormat without saving first
                BookFormat bookFormat = BookFormat.builder()
                        .book(savedBook)
                        .formatType(FormatType.valueOf(formatDto.formatType().toUpperCase()))
                        .purchasePrice(formatDto.purchasePrice())
                        .stockQuantity(formatDto.stockQuantity())
                        .isAvailableForPurchase(formatDto.isAvailableForPurchase())
                        .isAvailableForRental(formatDto.isAvailableForRental())
                        .isAvailableOnSubscription(formatDto.isAvailableOnSubscription())
                        .rentalOptions(new ArrayList<>())
                        .build();

                // Add rental options to the collection if provided
                if (formatDto.rentalOptions() != null && !formatDto.rentalOptions().isEmpty()) {
                    for (RentalOptionCreateDTO rentalOptionDto : formatDto.rentalOptions()) {
                        RentalOption rentalOption = RentalOption.builder()
                                .bookFormat(bookFormat)
                                .durationDays(rentalOptionDto.durationDays())
                                .price(rentalOptionDto.price())
                                .description(rentalOptionDto.description())
                                .build();

                        bookFormat.getRentalOptions().add(rentalOption);
                    }
                }

                // Now save the BookFormat with all rental options
                BookFormat savedBookFormat = bookFormatRepository.save(bookFormat);
                bookFormats.add(savedBookFormat);
            }
        }

        // Update the book with the formats
        savedBook.setAvailableFormats(bookFormats);

        // Convert to DTOs
        List<CategoryDTO> categoryDTOs = savedBook.getCategories().stream()
                .map(category -> new CategoryDTO(category.getId(), category.getName(), category.getDescription()))
                .collect(Collectors.toList());

        List<BookFormatDTO> formatDTOs = bookFormats.stream()
                .map(format -> new BookFormatDTO(
                        format.getId(),
                        format.getFormatType().name(),
                        format.getPurchasePrice(),
                        format.isAvailableForPurchase(),
                        format.isAvailableForRental(),
                        format.isAvailableOnSubscription(),
                        format.getRentalOptions().stream()
                                .map(rentalOption -> new RentalOptionDTO(
                                        rentalOption.getId(),
                                        rentalOption.getDurationDays(),
                                        rentalOption.getPrice(),
                                        rentalOption.getDescription()
                                ))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());

        // Convert to BookDetailDTO
        BookDetailDTO bookDetailDTO = new BookDetailDTO(
                savedBook.getId(),
                savedBook.getTitle(),
                savedBook.getAuthor(),
                savedBook.getIsbn(),
                savedBook.getDescription(),
                savedBook.getPublisher(),
                savedBook.getPublicationDate(),
                savedBook.getCoverImageUrl(),
                categoryDTOs,
                formatDTOs
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(bookDetailDTO);
    }

}