package com.b2g.catalogservice.controller;

import com.b2g.catalogservice.dto.BookSummaryDTO;
import com.b2g.catalogservice.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.PagedModel;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.*;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping({"", "/"})
    public ResponseEntity<?> getAllBooks(
            @RequestParam(required = false) Set<UUID> categoryIds,
            Pageable pageable) {
        return ResponseEntity.ok(Collections.emptyList());
    }

}