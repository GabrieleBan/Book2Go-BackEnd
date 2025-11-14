package com.b2g.reviewservice.controller;


import com.b2g.reviewservice.dto.RequestCreateReviewDTO;
import com.b2g.reviewservice.model.Review;
import com.b2g.reviewservice.service.JwtService;

import com.b2g.reviewservice.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final JwtService jwtService;
    private final ReviewService reviewService;

    @GetMapping({ "/{bookId}"})
    public ResponseEntity<?> getAllBooksReviews(
            @PathVariable UUID bookId) {
        List<Review> all_book_reviews = reviewService.getAllBookReviews(bookId);
        return ResponseEntity.status(HttpStatus.OK).body(all_book_reviews);
    }

    @PostMapping({"/"})
    public ResponseEntity<?> postBookReview(
            @RequestBody(required = true) @Valid RequestCreateReviewDTO review) {
//        modificare prendendo dai claims
        UUID userId=UUID.randomUUID();

        boolean review_created = reviewService.createBookReview(review,userId);
        return ResponseEntity.status(HttpStatus.OK).body(review_created);
    }

//    @GetMapping({"/"})
//    public ResponseEntity<?> postBookReview(){}
}
