package com.b2g.catalogservice.exceptions;

public class BookFormatNotFoundException extends RuntimeException {
    public BookFormatNotFoundException(String message) {
        super(message);
    }
}
