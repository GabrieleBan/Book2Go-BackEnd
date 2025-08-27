package com.b2g.catalogservice.repository;

import com.b2g.catalogservice.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {
    public Book findBookByIsbn(String isbn);
}
