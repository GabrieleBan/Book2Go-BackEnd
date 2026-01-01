package com.b2g.catalogservice.repository;

import com.b2g.catalogservice.model.Entities.CatalogBook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<CatalogBook, UUID> {
    CatalogBook findBookByIsbn(String isbn);

    // Metodo per filtrare libri per categorie con paginazione
    Page<CatalogBook> findByCategoriesIdIn(Set<UUID> categoryIds, Pageable pageable);
}
