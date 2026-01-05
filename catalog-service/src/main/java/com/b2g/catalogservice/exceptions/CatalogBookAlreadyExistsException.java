package com.b2g.catalogservice.exceptions;

public class CatalogBookAlreadyExistsException extends RuntimeException {
    public CatalogBookAlreadyExistsException(String message) {
        super(message);
    }
}
