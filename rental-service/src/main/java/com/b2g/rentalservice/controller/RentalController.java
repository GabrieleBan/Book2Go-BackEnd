package com.b2g.rentalservice.controller;

import com.b2g.commons.LendRequest;
import com.b2g.commons.RentalFormatCreationDTO;
import com.b2g.commons.RentalOptionCreateDTO;
import com.b2g.rentalservice.annotation.RequireRole;
import com.b2g.rentalservice.dto.RetrieveFormatsOptionsDTO;

import com.b2g.rentalservice.model.PhysicalBookIdentifier;
import com.b2g.rentalservice.model.RentalOption;
import com.b2g.rentalservice.service.PhysicalBookService;
import com.b2g.rentalservice.service.RentalService;
import com.b2g.commons.BookSummaryDTO;
import com.b2g.rentalservice.service.remoteJwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.*;
import jakarta.validation.Valid;

import java.util.*;
@Slf4j
@RestController
@RequestMapping("/rental")
@CrossOrigin("http://localhost:5173")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;
    private final remoteJwtService remoteJwtService;
    private final PhysicalBookService physicalBookService;
//    @RequireRole("ADMIN")
    @PostMapping("/format/create")
    ResponseEntity<?> createRentalFormat(@RequestBody @Valid RentalFormatCreationDTO formatDTO) {
        try {
            Set<PhysicalBookIdentifier> ids = rentalService.addRentalFormat(formatDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(ids);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
        }

    }

//    @RequireRole("ADMIN")
    @PostMapping("/format/add-option/{formatId}")
    ResponseEntity<?> addRentalOption(@PathVariable UUID formatId, @RequestBody @Valid RentalOptionCreateDTO optionDTO) {
        try {
            RentalOption rentalOption = rentalService.addOrCreateOptionTo(formatId, optionDTO);
            log.info("Rental option added {}", rentalOption);
            return ResponseEntity.status(HttpStatus.CREATED).body(rentalOption);
        }catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/format/{formatId}")
    ResponseEntity<?> getRentalOptions(@PathVariable UUID formatId) {
        try {
            RetrieveFormatsOptionsDTO options=rentalService.getFormatRentOptions(formatId);
            return ResponseEntity.status(HttpStatus.OK).body(options);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/")
    ResponseEntity<?> createLend(@RequestBody @Valid  LendRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getDetails() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
        System.out.println(SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        Claims claims = (Claims) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UUID userId = remoteJwtService.extractUserUUID(claims);
        try {
            rentalService.createNewlend(request,userId);
        }
        catch (Exception e) {return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());}



        return ResponseEntity.status(HttpStatus.CREATED).body("Lend started processing");

    }




}