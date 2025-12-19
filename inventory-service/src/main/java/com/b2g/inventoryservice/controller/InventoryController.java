package com.b2g.inventoryservice.controller;


import com.b2g.inventoryservice.dto.UpdateCopyStateRequest;
import com.b2g.inventoryservice.model.entities.ReservationRequest;
import com.b2g.inventoryservice.model.valueObjects.CopyId;
import com.b2g.inventoryservice.repository.ReservationsRepository;
import jakarta.validation.Valid;
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
    private final ReservationsRepository reservationsRepository;
    @PatchMapping(
            value = "/libraries/{libraryId}/physical-copies/{bookId}/{copyNumber}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> retrieveInventory(
            @PathVariable UUID bookId,
            @PathVariable UUID libraryId,
            @PathVariable Integer copyNumber,
            @RequestBody @Valid UpdateCopyStateRequest state
            ) {
        log.info("Retrieving inventory for book id {} a library {}", bookId, libraryId);

        ReservationRequest reserved= reservationsRepository.findByLibraryIDAndPhysicalBookIdentifier(libraryId,new CopyId(bookId, copyNumber));
        reservationsRepository.delete(reserved);
        return ResponseEntity.ok().body(reserved.getCopyId());
    }


}
