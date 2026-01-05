package com.b2g.inventoryservice.repository;

import com.b2g.inventoryservice.model.entities.ReferenceBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReferenceBookRepository extends JpaRepository<ReferenceBook, UUID> {
}
