package com.b2g.lendservice.controller;

import com.b2g.lendservice.annotation.RequireRole;
import com.b2g.lendservice.model.LendableCopy;
import com.b2g.lendservice.model.Lending;
import com.b2g.lendservice.service.LendsService;
import com.b2g.lendservice.service.remoteJwtService;
import io.jsonwebtoken.Claims;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/lend")
@RequiredArgsConstructor
public class LendingController{

    private final LendsService lendingService;
    private final remoteJwtService jwtService;

    @RequireRole("USER")
    @PostMapping("/{lendableBookId}/request")
    public ResponseEntity<?> requestLending(
            @PathVariable UUID lendableBookId,
            @RequestParam UUID libraryId
            ) throws Exception {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getDetails() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
        System.out.println(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        Claims claims = (Claims) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UUID userId = jwtService.extractUserUUID(claims);
        Lending lending = lendingService.requestLending(userId, lendableBookId, libraryId);
        return ResponseEntity.status(HttpStatus.CREATED).body(lending);
        }


    @RequireRole("EMPLOYEE")
    @PostMapping("/{lendableBookId}/assign")
    public ResponseEntity<?> assignPhysicalBook(@PathVariable UUID lendableBookId,
            @RequestParam @NotNull UUID userId,
            @RequestParam @NotNull UUID libraryId,
            @RequestParam @NotNull Integer copyNumber) {
        //questi dati potrei prenderli dai claims in futuro
    Lending lend = lendingService.giveLendCopyToUser(userId, libraryId, lendableBookId, copyNumber);
    return ResponseEntity.ok(lend);
    }

    @RequireRole("EMPLOYEE")
    @GetMapping("/user/{userId}/awaiting")
    public ResponseEntity<List<LendableCopy>> getReaderAwaitingLendCopies(@PathVariable UUID userId) {
        List<LendableCopy> lends = lendingService.getReaderAwaitingLends(userId);
        return ResponseEntity.ok(lends);
    }


}