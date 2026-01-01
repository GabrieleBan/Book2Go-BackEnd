package com.b2g.catalogservice.repository;

import com.b2g.catalogservice.model.Entities.BookFormat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookFormatRepository extends JpaRepository<BookFormat, UUID> {
    List<BookFormat> findByBookId(UUID bookId);
}
