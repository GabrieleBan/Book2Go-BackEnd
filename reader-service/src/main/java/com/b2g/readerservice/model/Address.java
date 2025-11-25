package com.b2g.readerservice.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @NotBlank(message = "La via non può essere vuota")
    private String street;

    @NotBlank(message = "La città non può essere vuota")
    private String city;

    @Pattern(regexp = "\\d{5}", message = "Il CAP italiano deve essere composto da 5 cifre")
    private String zip;
}