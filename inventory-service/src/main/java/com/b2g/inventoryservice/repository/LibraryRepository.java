package com.b2g.inventoryservice.repository;

import com.b2g.inventoryservice.model.entities.Library;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LibraryRepository extends JpaRepository<Library, UUID> {
}
