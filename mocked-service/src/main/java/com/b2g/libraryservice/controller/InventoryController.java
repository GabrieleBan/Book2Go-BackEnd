package com.b2g.libraryservice.controller;


import com.b2g.libraryservice.model.LendingBooksReservations;
import com.b2g.libraryservice.model.PhysicalBookIdentifier;
import com.b2g.libraryservice.repository.LendingBooksRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final LendingBooksRepository lendingBooksRepository;
    @GetMapping("/lendable-books/retrieve")
    public ResponseEntity<?> retrieveInventory(
            @RequestParam UUID bookId,@RequestParam UUID libraryId,@RequestParam Integer id
            ) {
        log.info("Retrieving inventory for book id {} a library {}", bookId, libraryId);

        LendingBooksReservations reserved=lendingBooksRepository.findByLibraryIDAndPhysicalBookIdentifier(libraryId,new PhysicalBookIdentifier(id,bookId));
        lendingBooksRepository.delete(reserved);
        return ResponseEntity.ok().body(reserved.getPhysicalBookIdentifier());
    }


}
