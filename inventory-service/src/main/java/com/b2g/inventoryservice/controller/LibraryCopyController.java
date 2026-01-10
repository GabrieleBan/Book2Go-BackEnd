package com.b2g.inventoryservice.controller;


import com.b2g.inventoryservice.annotation.RequireRole;
import com.b2g.inventoryservice.dto.StockUpdateRequest;
import com.b2g.inventoryservice.dto.createLibraryCopyFromStockdto;
import com.b2g.inventoryservice.model.entities.LibraryCopy;
import com.b2g.inventoryservice.model.entities.RetailStock;
import com.b2g.inventoryservice.service.applicationService.InventoryApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/library-copy")
@RequiredArgsConstructor
public class LibraryCopyController {
    private final InventoryApplicationService inventoryApplicationService;

    @RequireRole("ADMIN")
    @PostMapping({"/"
            ,""})
    public ResponseEntity<LibraryCopy> getBookAvailabilityInThisLibrary(@RequestBody createLibraryCopyFromStockdto request) {
        LibraryCopy copy = inventoryApplicationService.createLibraryCopyFromStock(
                request
        );

        return ResponseEntity.ok().body(copy);

    }

}