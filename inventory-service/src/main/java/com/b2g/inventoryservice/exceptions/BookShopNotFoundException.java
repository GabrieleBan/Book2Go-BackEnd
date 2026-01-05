package com.b2g.inventoryservice.exceptions;

public class BookShopNotFoundException extends RuntimeException {
    public BookShopNotFoundException(String message) {
        super(message);
    }
}
