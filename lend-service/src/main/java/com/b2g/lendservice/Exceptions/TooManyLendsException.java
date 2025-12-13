package com.b2g.lendservice.Exceptions;

public class TooManyLendsException extends RuntimeException {
    public TooManyLendsException(String message) {
        super(message);
    }
}
