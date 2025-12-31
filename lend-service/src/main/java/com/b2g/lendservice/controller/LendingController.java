package com.b2g.lendservice.controller;

import com.b2g.lendservice.annotation.RequireRole;
import com.b2g.lendservice.dto.LendableCopyEntrustRequest;
import com.b2g.lendservice.dto.LendingRequest;
import com.b2g.lendservice.model.entities.Lending;
import com.b2g.lendservice.service.application.LendingsApplicationService;
import com.b2g.lendservice.service.infrastructure.remoteJwtService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/lendings")
@RequiredArgsConstructor
public class LendingController{

    private final LendingsApplicationService lendingService;
    private final remoteJwtService jwtService;

    @RequireRole("READER")
    @PostMapping("")
    public ResponseEntity<?> requestLending(
            @RequestBody @Valid LendingRequest request
            ) throws Exception {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getDetails() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
        System.out.println(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        Claims claims = (Claims) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UUID userId = jwtService.extractUserUUID(claims);
        Lending lending = lendingService.requestLending(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(lending);
        }


    @RequireRole("EMPLOYEE")
    @PostMapping("/{lendingId}/assignment")
    public ResponseEntity<?> entrustCopy(
            @PathVariable UUID lendingId,
           @RequestBody LendableCopyEntrustRequest requestEntrust
    ) {
        //questi dati potrei prenderli dai claims in futuro
    Lending lend = lendingService.entrustLendCopyToUser(lendingId,requestEntrust);
    return ResponseEntity.ok(lend);
    }







}