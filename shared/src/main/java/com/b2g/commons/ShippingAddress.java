package com.b2g.commons;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Questa ci permette di far sì che l'indirizzo di spedizione sia parte della tabella ordini anziché una tabella a sé stante
// (non ha senso avere un indirizzo di spedizione senza un ordine associato in questo contesto)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingAddress {
    private String street;
    private String city;
    private String zipCode;
    private String country;
    private String stateOrProvince;
    private String fullName;
    private String phoneNumber;
}