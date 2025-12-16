package com.b2g.lendservice.controller;

import com.b2g.commons.LendState;
import com.b2g.lendservice.annotation.RequireRole;
import com.b2g.lendservice.model.LendableCopy;
import com.b2g.lendservice.model.Lending;
import com.b2g.lendservice.service.LendsService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/lendings/readers")
@RequiredArgsConstructor
public class ReaderLendingController {
    private final LendsService lendingService;
    @RequireRole("EMPLOYEE")
    @GetMapping("/{readerId}/lendable-copies")
    public ResponseEntity<List<LendableCopy>> getReaderAwaitingLendCopies(
            @PathVariable UUID readerId,
            @RequestParam(required = false) UUID libraryId //si puo spostare nei claims poi
    ) {
        List<LendableCopy> lends = lendingService.getReaderAwaitingLendCopies(readerId,libraryId);
        return ResponseEntity.ok(lends);
    }

    @RequireRole("EMPLOYEE")
    @GetMapping("/{readerId}/lendings")
    public ResponseEntity<List<Lending>> getReaderLends(@PathVariable UUID readerId, @RequestParam @NotNull Set<LendState> states) {
        List<Lending> lends = lendingService.getReaderLendings(readerId,states);
        return ResponseEntity.ok(lends);
    }

}