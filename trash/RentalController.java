package com.b2g.lendservice.controller;

import com.b2g.commons.LendRequest;
import com.b2g.commons.RentalFormatCreationDTO;
import com.b2g.commons.RentalOptionCreateDTO;
import com.b2g.lendservice.annotation.RequireRole;
import com.b2g.lendservice.dto.RentalFormatDto;
import com.b2g.lendservice.dto.RetrieveFormatsOptionsDTO;

import com.b2g.lendservice.model.LendableBook;
import com.b2g.lendservice.model.Lending;
import com.b2g.lendservice.model.PhysicalBookIdentifier;
import com.b2g.lendservice.model.LendingOption;

import com.b2g.lendservice.service.*;
import io.jsonwebtoken.Claims;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.*;
@Slf4j
@RestController
@RequestMapping("/lend")
@CrossOrigin("http://localhost:5173")
@RequiredArgsConstructor
public class RentalController {

//    private final RentalService rentalService;
    private final LendingApplicationService lendsService;
    private final LendableBookApplicationService lendableBookService;
    private final remoteJwtService remoteJwtService;

//    @RequireRole("ADMIN")
//    @PostMapping("/format/")
//    ResponseEntity<?> createNewLendableFormat(@RequestParam UUID bookId,@RequestParam UUID formatId) throws Exception {
//        LendableBook newBook= lendableBookService.createLendableBook(bookId,formatId);
//        return ResponseEntity.status(HttpStatus.CREATED).body(newBook);
//    }

//    @RequireRole("ADMIN")
//    @PostMapping("/internal/format/add-option/{formatId}")
//    ResponseEntity<?> addRentalOption(@PathVariable UUID formatId, @RequestBody @Valid RentalOptionCreateDTO optionDTO) {
//        try {
//            log.info("adding rental option with id {}", formatId);
//            LendingOption lendingOption = rentalService.addOrCreateOptionTo(formatId, optionDTO);
//            log.info("Rental option added {}", lendingOption);
//            return ResponseEntity.status(HttpStatus.CREATED).body(lendingOption);
//        }catch (Exception e) {
//            log.error(e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
//        }
//    }
//
//    @GetMapping("/format/{formatId}/options")
//    ResponseEntity<?> getRentalOptions(@PathVariable UUID formatId) {
//        try {
//            RetrieveFormatsOptionsDTO options=rentalService.getFormatRentOptions(formatId);
//            return ResponseEntity.status(HttpStatus.OK).body(options);
//        }catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
//        }
//    }
//
//    @GetMapping("/format/{bookId}")
//    ResponseEntity<?> getRentableFormats(@PathVariable UUID bookId) {
//        try {
//            List<RentalFormatDto> formats=rentalService.getRentableFormats(bookId);
//            return ResponseEntity.status(HttpStatus.OK).body(formats);
//        }catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
//        }
//    }

    @PostMapping({"/request","/request/"})
    ResponseEntity<?> createLendFromRemote(@RequestBody @Valid  LendRequest request) {
//        sia lend che rent
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
    @RequireRole({"ADMIN", "EMPLOYEE"})
    @PostMapping({"/entrust","/entrust/"})
    ResponseEntity<?> entrustLendToReader(
            @RequestParam @NotNull(message = "readerId that must receive the lend must be specified") UUID readerId,
            @RequestParam @NotNull(message = "library Id cannot be empty") UUID libraryId,
            @RequestParam @NotNull(message = "format Id cannot be empty") UUID formatId,
            @RequestParam @NotNull(message = "physical book id cannot be empty") Integer id) {
        try {
            PhysicalBookIdentifier physLendingBookId=rentalService.givePhysicalBookToReader(readerId,libraryId,formatId,id);
            return ResponseEntity.status(HttpStatus.OK).body(physLendingBookId);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
        }
    }
    @RequireRole({"ADMIN", "EMPLOYEE"})
    @PostMapping({"/reader/awaiting","/reader/awaiting/"})
    ResponseEntity<?> getReaderLendBooksToGive(
            @RequestParam @NotNull(message = "readerId that must receive the lend must be specified") UUID readerId
) {
        try {
            List<PhysicalBookIdentifier> physLendingBookId=rentalService.getReaderAwaitingRetrievalLends(readerId);
            return ResponseEntity.status(HttpStatus.OK).body(physLendingBookId);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
        }
    }

}