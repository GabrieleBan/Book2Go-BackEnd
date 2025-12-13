package com.b2g.lendservice.repository;

import com.b2g.lendservice.model.LendableBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LendableBookRepository extends JpaRepository<LendableBook, UUID> {
    List<LendableBook> findByBookId(UUID bookId);

    LendableBook findByFormatId(UUID formatId);

    List<LendableBook> findAllByBookId(UUID bookId);
}