package com.b2g.inventoryservice.exceptions;

public class StockQuantityException extends RuntimeException {
    public StockQuantityException(String s) {
        super(s);
    }
}
