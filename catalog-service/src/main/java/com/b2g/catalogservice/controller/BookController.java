package com.b2g.catalogservice.controller;

import com.b2g.catalogservice.annotation.RequireRole;
import com.b2g.catalogservice.dto.*;
import com.b2g.catalogservice.model.BookFormat;
import com.b2g.catalogservice.service.BookService;
import com.b2g.commons.BookSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.*;
import jakarta.validation.Valid;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/books")
@CrossOrigin("http://localhost:5173")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping({"", "/"})
    public ResponseEntity<List<BookSummaryDTO>> getAllBooks(
            @RequestParam(required = false) Set<UUID> categoryIds,
            Pageable pageable) {
        List<BookSummaryDTO> books = bookService.getAllBooks(categoryIds, pageable);
        return ResponseEntity.ok(books);
    }

    @PostMapping({"", "/"})
    @RequireRole("ADMIN")
    public ResponseEntity<?> createBook(@RequestBody @Valid BookCreateRequestDTO request) {
        BookDetailDTO createdBook;
        try {
            createdBook = bookService.createBook(request);
        }
        catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        }

        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDetailDTO> getBookById(@PathVariable UUID id) {
        Optional<BookDetailDTO> book = bookService.getBookById(id);

        return book.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{bookId}/formats/{formatId}")
    public ResponseEntity<?> getBookFormat(
            @PathVariable UUID bookId,
            @PathVariable UUID formatId
    ) {
        try {
            CatalogFormatResponse dto=bookService.getBooksFormat(bookId,formatId);
            return ResponseEntity.ok(dto);
        }catch (NoSuchElementException e) {return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());}


    }

}