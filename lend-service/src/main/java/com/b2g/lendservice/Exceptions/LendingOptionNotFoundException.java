package com.b2g.lendservice.Exceptions;

import java.util.UUID;

public class LendingOptionNotFoundException extends RuntimeException {
    public LendingOptionNotFoundException(UUID optionId) {
        super("Lending option with id " + optionId + " not found");
    }
}