package com.b2g.orderservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Questa ci permette di far sì che l'indirizzo di spedizione sia parte della tabella ordini anziché una tabella a sé stante
// (non ha senso avere un indirizzo di spedizione senza un ordine associato in questo contesto)
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingAddress {
    @Column(name = "shipping_street")
    private String street;

    @Column(name = "shipping_city")
    private String city;

    @Column(name = "shipping_zip_code")
    private String zipCode;

    @Column(name = "shipping_country")
    private String country;

    @Column(name = "shipping_state_or_province")
    private String stateOrProvince;

    @Column(name = "shipping_full_name")
    private String fullName;

    @Column(name = "shipping_phone_number")
    private String phoneNumber;
}