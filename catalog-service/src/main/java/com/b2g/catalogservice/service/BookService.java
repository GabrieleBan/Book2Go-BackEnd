package com.b2g.catalogservice.service;

import com.b2g.catalogservice.dto.*;
import com.b2g.catalogservice.model.*;
import com.b2g.catalogservice.repository.BookRepository;
import com.b2g.catalogservice.repository.CategoryRepository;
import com.b2g.catalogservice.repository.BookFormatRepository;
import com.b2g.catalogservice.repository.RentalOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final BookFormatRepository bookFormatRepository;
    private final RentalOptionRepository rentalOptionRepository;

    public List<BookSummaryDTO> getAllBooks(Set<UUID> categoryIds, Pageable pageable) {
        Page<Book> booksPage;

        // Se non ci sono filtri per categoria, prendi tutti i libri con paginazione
        if (categoryIds == null || categoryIds.isEmpty()) {
            booksPage = bookRepository.findAll(pageable);
        } else {
            // Filtra i libri per le categorie specificate
            booksPage = bookRepository.findByCategoriesIdIn(categoryIds, pageable);
        }

        return booksPage.getContent().stream()
                .map(this::convertToBookSummaryDTO)
                .collect(Collectors.toList());
    }

    public BookDetailDTO createBook(BookCreateRequestDTO request) {
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
        List<BookFormat> bookFormats = createBookFormats(request.formats(), savedBook);

        // Update the book with the formats
        savedBook.setAvailableFormats(bookFormats);

        return convertToBookDetailDTO(savedBook, bookFormats);
    }

    private List<BookFormat> createBookFormats(List<BookFormatCreateDTO> formatDtos, Book book) {
        List<BookFormat> bookFormats = new ArrayList<>();

        if (formatDtos != null && !formatDtos.isEmpty()) {
            for (BookFormatCreateDTO formatDto : formatDtos) {
                BookFormat bookFormat = BookFormat.builder()
                        .book(book)
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

                // Save the BookFormat with all rental options
                BookFormat savedBookFormat = bookFormatRepository.save(bookFormat);
                bookFormats.add(savedBookFormat);
            }
        }

        return bookFormats;
    }

    private BookDetailDTO convertToBookDetailDTO(Book book, List<BookFormat> bookFormats) {
        // Convert to DTOs
        List<CategoryDTO> categoryDTOs = book.getCategories().stream()
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

        return new BookDetailDTO(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getDescription(),
                book.getPublisher(),
                book.getPublicationDate(),
                book.getCoverImageUrl(),
                categoryDTOs,
                formatDTOs
        );
    }

    private BookSummaryDTO convertToBookSummaryDTO(Book book) {
        List<CategoryDTO> categoryDTOs = book.getCategories().stream()
                .map(category -> new CategoryDTO(category.getId(), category.getName(), category.getDescription()))
                .collect(Collectors.toList());

        return new BookSummaryDTO(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getCoverImageUrl(),
                categoryDTOs
        );
    }

    public Optional<BookDetailDTO> getBookById(UUID id) {
        Optional<Book> bookOptional = bookRepository.findById(id);

        if (bookOptional.isPresent()) {
            Book book = bookOptional.get();
            return Optional.of(convertToBookDetailDTO(book, book.getAvailableFormats()));
        }

        return Optional.empty();
    }
}
