package com.b2g.lendservice.advice;


import com.b2g.lendservice.Exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LendableBookException.class)
    public ResponseEntity<ErrorResponse> handleLendableBook(LendableBookException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(BookAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleBookAlreadyExists(BookAlreadyExistsException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(TooManyLendsException.class)
    public ResponseEntity<ErrorResponse> handleTooManyLends(TooManyLendsException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(LendingOptionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLendingOptionNotFound(LendingOptionNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(SessionAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleSessionAuth(SessionAuthenticationException ex) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), message));
    }

    public record ErrorResponse(int status, String message) {}
}