package com.b2g.catalogservice.controller;

import com.b2g.catalogservice.annotation.RequireRole;
import com.b2g.catalogservice.dto.*;
import com.b2g.catalogservice.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.*;

import java.util.*;

@RestController
@RequestMapping("/books")
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
    public ResponseEntity<BookDetailDTO> createBook(@RequestBody /* @Valid */ BookCreateRequestDTO request) {
        BookDetailDTO createdBook = bookService.createBook(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDetailDTO> getBookById(@PathVariable UUID id) {
        Optional<BookDetailDTO> book = bookService.getBookById(id);

        return book.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

}