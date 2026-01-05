package com.b2g.catalogservice.service.application;
import com.b2g.catalogservice.dto.BookSummaryDTO;
import com.b2g.catalogservice.dto.CatalogBookCreateRequestDTO;
import com.b2g.catalogservice.model.Entities.CatalogBook;
import com.b2g.catalogservice.model.VO.Category;
import com.b2g.catalogservice.repository.BookRepository;
import com.b2g.catalogservice.repository.CategoryRepository;
import com.b2g.catalogservice.service.domain.CatalogBookDomainService;
import com.b2g.catalogservice.exceptions.CatalogBookNotFoundException;
import com.b2g.catalogservice.service.infrastructure.CatalogEventPublisher;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class CatalogBookApplicationService {

    private final CatalogBookDomainService catalogBookDomainService;
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final CatalogEventPublisher catalogEventPublisher;

    @Transactional
    public BookSummaryDTO createCatalogBook(CatalogBookCreateRequestDTO request) {
        // Recupero categorie dai repository
        Set<Category> categories = new HashSet<>(categoryRepository.findAllById(request.categoryIds()));

        // Chiamo il domain service per creare l'entità
        CatalogBook catalogBook = catalogBookDomainService.createCatalogBook(
                request.title(),
                request.edition(),
                request.author(),
                request.description(),
                request.publisher(),
                request.publicationDate(),
                categories
        );
        catalogBook= bookRepository.save(catalogBook);
        try {
            catalogEventPublisher.publishCatalogBookCreatedEvent(catalogBook);
        } catch (Exception e) {
            throw new RuntimeException("Errore durante il signaling: " + e.getMessage(), e);
        }

        // Persiste tramite repository
        return BookSummaryDTO.fromCatalogBook(catalogBook);
    }


    public BookSummaryDTO getCatalogBookSummaryById(UUID bookId) {
        CatalogBook book= bookRepository.findById(bookId).orElse(null);
        if (book == null) {
            throw new CatalogBookNotFoundException("Libro non trovato nel catalogo");
        }
        return BookSummaryDTO.fromCatalogBook(book);
    }
    public CatalogBook getCatalogBookById(UUID bookId) {
        CatalogBook book= bookRepository.findById(bookId).orElse(null);
        if (book == null) {
            throw new CatalogBookNotFoundException("Libro non trovato nel catalogo");
        }
        return book;
    }

    public Page<BookSummaryDTO> getBooksByCategories(Set<UUID> categoryIds, Pageable pageable) {
        Page<CatalogBook> booksPage;

        if (categoryIds == null || categoryIds.isEmpty()) {
            booksPage = bookRepository.findAll(pageable);
        } else {
            booksPage = bookRepository.findByCategoriesIdIn(categoryIds, pageable);
        }


        List<BookSummaryDTO> dtoList = booksPage.getContent().stream()
                .map(BookSummaryDTO::fromCatalogBook)
                .collect(Collectors.toList());


        return new PageImpl<>(dtoList, pageable, booksPage.getTotalElements());
    }

    public Page<CatalogBook> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }


}