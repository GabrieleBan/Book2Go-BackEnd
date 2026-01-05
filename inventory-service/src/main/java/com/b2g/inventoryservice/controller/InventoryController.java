package com.b2g.inventoryservice.controller;


import com.b2g.inventoryservice.annotation.RequireRole;
import com.b2g.inventoryservice.dto.StockUpdateRequest;
import com.b2g.inventoryservice.model.entities.LibraryCopy;
import com.b2g.inventoryservice.model.entities.RetailStock;
import com.b2g.inventoryservice.service.applicationService.InventoryApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryApplicationService inventoryApplicationService;
    @RequireRole({"EMPLOYEE","ADMIN"})
    @PatchMapping(
            value = "/libraries/{libraryId}/physical-copies/{bookId}/{copyNumber}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> retrieveInventory(
            @PathVariable UUID bookId,
            @PathVariable UUID libraryId,
            @PathVariable Integer copyNumber
            ) {
        log.info("Retrieving inventory for book id {} a library {}", bookId, libraryId);
        LibraryCopy copy= inventoryApplicationService.retrieveCopy(libraryId,bookId,copyNumber);
        return ResponseEntity.ok().body(copy);
    }

    @RequireRole({"EMPLOYEE","ADMIN"})
    @PatchMapping(
            value = "/stocks/libraries/{libraryId}/books/{bookId}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RetailStock> updateStock(
            @PathVariable UUID libraryId,
            @PathVariable UUID bookId,
            @RequestBody StockUpdateRequest request
    ) {
        log.info("Updating stock for book {} in library {} by {}",
                bookId, libraryId, request.quantity);
        RetailStock stock =inventoryApplicationService.changeStockQuantity(
                    bookId, libraryId, request.quantity
            );

        return ResponseEntity.ok().body(stock);
    }


}
