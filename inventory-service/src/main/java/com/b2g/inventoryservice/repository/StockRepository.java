package com.b2g.inventoryservice.repository;

import com.b2g.inventoryservice.model.entities.RetailStock;
import com.b2g.inventoryservice.model.valueObjects.StockId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StockRepository extends JpaRepository<RetailStock, StockId> {
    List<RetailStock> findById_BookId(UUID idBookId);
}
