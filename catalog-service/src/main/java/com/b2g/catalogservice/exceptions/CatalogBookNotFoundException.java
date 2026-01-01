package com.b2g.catalogservice.exceptions;

public class CatalogBookNotFoundException extends RuntimeException {
    public CatalogBookNotFoundException(String message) {
        super(message);
    }
}
