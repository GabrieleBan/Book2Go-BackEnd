package com.b2g.lendservice.Exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String userIdNonTrovato) {
        super(userIdNonTrovato);
    }
}
