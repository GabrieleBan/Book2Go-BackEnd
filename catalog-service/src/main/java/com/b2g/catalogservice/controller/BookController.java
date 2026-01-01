package com.b2g.catalogservice.controller;

import com.b2g.catalogservice.annotation.RequireRole;
import com.b2g.catalogservice.dto.*;
import com.b2g.catalogservice.model.Entities.BookFormat;

import com.b2g.catalogservice.service.application.BookFormatApplicationService;
import com.b2g.catalogservice.service.application.CatalogBookApplicationService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.*;
import jakarta.validation.Valid;

import java.util.*;

@RestController
@RequestMapping("/books")
@CrossOrigin("http://localhost:5173")
@RequiredArgsConstructor
public class BookController {

    private final CatalogBookApplicationService catalogBookService;
    private final BookFormatApplicationService bookFormatService;

    @GetMapping({"", "/"})
    public ResponseEntity<Page<BookSummaryDTO>> getAllBooks(
            @RequestParam(required = false) Set<UUID> categoryIds,
            Pageable pageable) {
        Page<BookSummaryDTO> books = catalogBookService.getBooksByCategories(categoryIds, pageable);
        return ResponseEntity.ok(books);
    }

    @PostMapping({"", "/"})
    @RequireRole("ADMIN")
    public ResponseEntity<BookSummaryDTO> createBook(@RequestBody @Valid CatalogBookCreateRequestDTO request) {
        BookSummaryDTO createdBook = catalogBookService.createCatalogBook(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookSummaryDTO> getBookById(@PathVariable UUID id) {
        BookSummaryDTO book = catalogBookService.getCatalogBookSummaryById(id);
        return ResponseEntity.ok(book);
    }

    @GetMapping("/formats/{formatId}")
    public ResponseEntity<BookFormat> getBookFormat(
            @PathVariable UUID formatId) {
        BookFormat dto = bookFormatService.getBookFormat(formatId);
        return ResponseEntity.ok(dto);
    }
    @GetMapping("/{bookId}/formats")
    public ResponseEntity<List<BookFormat>> getCatalogBookFormats(
            @PathVariable UUID bookId) {
        List<BookFormat> dto = bookFormatService.getBookFormats(bookId);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{bookId}/formats")
    @RequireRole("ADMIN")
    public ResponseEntity<BookFormat> createBookFormat(
            @PathVariable UUID bookId,
            @RequestBody @Valid BookFormatCreateDTO request) {
        BookFormat createdFormat = bookFormatService.createBookFormat(bookId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFormat);
    }
}