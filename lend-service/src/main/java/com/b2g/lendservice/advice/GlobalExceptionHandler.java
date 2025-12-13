package com.b2g.lendservice.advice;


import com.b2g.lendservice.Exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Gestione eccezioni custom per LendableBook
    @ExceptionHandler(LendableBookException.class)
    public ResponseEntity<ErrorResponse> handleLendableBookException(LendableBookException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Gestione eccezioni custom per prestiti già esistenti
    @ExceptionHandler(BookAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleBookAlreadyExists(BookAlreadyExistsException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // Gestione eccezioni custom per limite prestiti arrivato
    @ExceptionHandler(TooManyLendsException.class)
    public ResponseEntity<ErrorResponse> handleTooManyLendsException(TooManyLendsException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Gestione generale per tutte le altre eccezioni
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    @ExceptionHandler(LendingOptionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLendingOptionNotFound(LendingOptionNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // DTO semplice per la risposta di errore
    public static record ErrorResponse(int status, String message) {}
}