package com.b2g.catalogservice.service.domain;

import com.b2g.catalogservice.exceptions.CategoryNotFoundException;
import com.b2g.catalogservice.model.Entities.CatalogBook;
import com.b2g.catalogservice.model.VO.Category;
import com.b2g.catalogservice.repository.BookRepository;
import com.b2g.catalogservice.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogBookDomainService {

    @Transactional
    public CatalogBook createCatalogBook(
            String title,
            String author,
            String description,
            String publisher,
            LocalDate publicationDate,
            Set<Category> categories
    ) {
        if (categories == null || categories.isEmpty()) {
            throw new CategoryNotFoundException("At least one category must be specified");
        }

        // Usa il factory method della entity
        return CatalogBook.create(
                title,
                author,
                description,
                publisher,
                publicationDate,
                categories
        );
    }
}



