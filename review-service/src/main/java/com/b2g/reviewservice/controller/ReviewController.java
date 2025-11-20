package com.b2g.reviewservice.controller;


import com.b2g.reviewservice.dto.RequestCreateReviewDTO;
import com.b2g.reviewservice.model.Review;
import com.b2g.reviewservice.service.JwtService;

import com.b2g.reviewservice.service.ReviewService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@CrossOrigin("http://localhost:5173/")
public class ReviewController {
    private final JwtService jwtService;
    private final ReviewService reviewService;

    @GetMapping("/{bookId}")
    public ResponseEntity<?> getAllBooksReviews(
            @PathVariable UUID bookId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("postedDate").descending()); // esempio: ordinamento per rating decrescente
        Page<Review> allBookReviews = reviewService.getAllBookReviews(bookId, pageable);

        return ResponseEntity.ok(allBookReviews);
    }

    @PostMapping({"/"})
    public ResponseEntity<?> postBookReview(
            @RequestBody(required = true) @Valid RequestCreateReviewDTO review,@RequestHeader("Authorization") String authHeader) {
//        modificare prendendo dai claims
        String jwt = authHeader.substring(7);
        if (jwt.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
        System.out.println("jwt: " + jwt);
        Claims claims = jwtService.validateToken(jwt);
        UUID userId = jwtService.extractUserUUID(claims);


        boolean review_created = reviewService.createBookReview(review,userId);
        return ResponseEntity.status(HttpStatus.OK).body(review_created);
    }

//    @GetMapping({"/"})
//    public ResponseEntity<?> postBookReview(){}
}
