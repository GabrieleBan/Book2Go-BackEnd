package com.b2g.lendservice.Exceptions;

public class InfrastructureException extends RuntimeException {
    public InfrastructureException(String servizioAbbonamentiNonRisponde) {
        super(servizioAbbonamentiNonRisponde);
    }
}
