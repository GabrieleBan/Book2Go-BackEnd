package com.b2g.readerservice.controller;

import com.b2g.commons.SubscriptionType;
import com.b2g.readerservice.dto.ReaderForm;
import com.b2g.readerservice.dto.ReaderPublicInfo;
import com.b2g.readerservice.dto.ReaderSummary;
import com.b2g.readerservice.model.Reader;
import com.b2g.readerservice.service.ReaderService;
import com.b2g.readerservice.service.remoteJwtService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.management.InstanceAlreadyExistsException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/readers")
@RequiredArgsConstructor
public class ReaderController {
    private final ReaderService readerService;
    private final remoteJwtService  remoteJwtService;


//    @PostMapping("/changeInfo")
//    public ResponseEntity<?> setInfo(@Valid @RequestBody ReaderSettableInfo addInfo, @RequestHeader("Authorization") String authHeader) {
//
//        return
//    }

    @GetMapping("/sum-info")
    public ResponseEntity<?> getReadersSummary(@RequestParam(required = true) Set<UUID> readersIds) {
        if (readersIds == null || readersIds.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body("No Readers Id specified");

        }
        List<ReaderSummary> summaries=readerService.retrieveReadersSummary(readersIds);
        System.out.println(summaries.toString());
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/all-info/{userId}")
    public ResponseEntity<?> getReaderInfo(@PathVariable UUID userId) {

        ReaderPublicInfo info= readerService.retrieveReaderFullPublicInfo(userId);
        return ResponseEntity.ok(info);
    }

    @GetMapping("/me/info")
    public ResponseEntity<?> getMyReaderInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        if (auth == null || auth.getDetails() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }


        Claims claims = (Claims) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId = remoteJwtService.extractUserUUID(claims);
        Reader info=readerService.retrieveReaderById(userId);
        return ResponseEntity.ok(info);
    }

    @PutMapping("/me/fill-form-info")
    public ResponseEntity<?> putMyReaderInfo(@RequestBody @Valid ReaderForm readerForm) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        if (auth == null || auth.getDetails() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }


        Claims claims = (Claims) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId = remoteJwtService.extractUserUUID(claims);


        Reader info= null;
        try {
            info = readerService.addReaderOneTimeInfo(userId,readerForm);

        } catch (InstanceAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
        return ResponseEntity.ok(info);

    }

    @PutMapping("/me/description")
    public ResponseEntity<?> putReaderInfo(@RequestBody String new_description) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        if (auth == null || auth.getDetails() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }

        Claims claims = (Claims) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId = remoteJwtService.extractUserUUID(claims);

        readerService.changeReaderDescription(userId,new_description);
        return ResponseEntity.ok(new_description);


    }

    @GetMapping("/{readerId}/check/subscription")
    public ResponseEntity<?> getReaderSubscriptionType(@PathVariable UUID readerId) {
        try {
            SubscriptionType subscriptionType=readerService.getReaderSubscription(readerId);
            return ResponseEntity.ok(subscriptionType);
        }catch (Exception e)
        {
            log.error("Error getting subscription type for reader with id {}",readerId,e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }




}
