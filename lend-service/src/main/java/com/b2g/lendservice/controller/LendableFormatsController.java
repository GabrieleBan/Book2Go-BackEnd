package com.b2g.lendservice.controller;

import com.b2g.lendservice.Exceptions.LendableBookException;
import com.b2g.lendservice.annotation.RequireRole;
import com.b2g.lendservice.dto.LendingOptionDTO;
import com.b2g.lendservice.model.LendableBook;
import com.b2g.lendservice.model.LendingOption;
import com.b2g.lendservice.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;


@RestController
@RequestMapping("/lendable-formats")
@RequiredArgsConstructor
public class LendableFormatsController {

    private final BookService bookService;

    /** CREA UN NUOVO LENDABLE FORMAT (verifca bookId + formatId richiesta al catalogg service) */
    @RequireRole("ADMIN")
    @PostMapping
    @Transactional
    public ResponseEntity<LendableBook> createNewLendableFormat(
            @RequestParam UUID bookId,
            @RequestParam UUID formatId) throws Exception {

        LendableBook newFormat = bookService.createLendableBook(bookId, formatId);
        return ResponseEntity.status(HttpStatus.CREATED).body(newFormat);
    }

    /** Ritorna tutti i lendable formats di un libro (inteso come archetype book) */
    @GetMapping
    public ResponseEntity<List<LendableBook>> getLendableFormatsForBook(
            @RequestParam UUID bookId) {

        List<LendableBook> formats = bookService.getLendableFormatsForBook(bookId);
        return ResponseEntity.ok(formats);
    }

    /** AGGIUNGE UN'OPZIONE DI PRESTITO A UN FORMATO PRESTABILE */
    @RequireRole("ADMIN")
    @PostMapping("/{formatId}/options")
    public ResponseEntity<Set<LendingOption>> addOptionToFormat(
            @PathVariable UUID formatId,
            @RequestBody LendingOptionDTO requestedOption) throws LendableBookException {

        Set<LendingOption> newOption = bookService.addLendingOption(formatId, requestedOption);
        return ResponseEntity.status(HttpStatus.CREATED).body(newOption);
    }

    /** RIMUOVE UN'OPZIONE DI PRESTITO DA UN FORMATO */
    @RequireRole("ADMIN")
    @DeleteMapping("/{formatId}/options/{optionId}")
    public ResponseEntity<Void> removeOptionFromFormat(
            @PathVariable UUID formatId,
            @PathVariable UUID optionId) {
        bookService.removeLendingOption(formatId, optionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{formatId}/options")
    public ResponseEntity<Set<LendingOption>> getOptionsForFormat(@PathVariable UUID formatId) {

        Set<LendingOption> options = bookService.getOptionsForLendableFormat(formatId);

        return ResponseEntity.ok(options);
    }
}