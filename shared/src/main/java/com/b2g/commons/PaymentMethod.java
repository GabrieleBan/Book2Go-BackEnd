package com.b2g.commons;

public class PaymentMethod {

    private String id;
    private PaymentMethodType type; // ENUM
    private String details;         // Es. ultime cifre carta, email PayPal ecc.
    private boolean isDefault;
}