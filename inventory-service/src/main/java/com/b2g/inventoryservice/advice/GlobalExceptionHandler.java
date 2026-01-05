package com.b2g.inventoryservice.advice;

import com.b2g.inventoryservice.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AvailabilityException.class)
    public ResponseEntity<ErrorResponse> handleAvailability(AvailabilityException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(BookShopNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBookShopNotFound(BookShopNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(LibraryCopyException.class)
    public ResponseEntity<ErrorResponse> handleLibraryCopy(LibraryCopyException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ReservationException.class)
    public ResponseEntity<ErrorResponse> handleReservation(ReservationException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ReservationRequestException.class)
    public ResponseEntity<ErrorResponse> handleReservationRequest(ReservationRequestException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(StockException.class)
    public ResponseEntity<ErrorResponse> handleStock(StockException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(StockQuantityException.class)
    public ResponseEntity<ErrorResponse> handleStockQuantity(StockQuantityException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity
                .status(status)
                .body(new ErrorResponse(status.value(), message));
    }

    public record ErrorResponse(int status, String message) {}
}