package com.b2g.readerservice.dto;

import com.b2g.readerservice.model.Address;
import jakarta.persistence.Embedded;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class ReaderForm {
    @NotBlank(message = "Il nome non può essere vuoto")
    private String name;

    @NotBlank(message = "Il cognome non può essere vuoto")
    private String surname;
    @NotNull
    @Length(min = 11,max = 15)
    @Pattern(regexp = "\\d+", message = "Il numero deve contenere solo cifre")
    private String phone;
    @NotNull
    @Valid
    @Embedded
    private Address address;
    private String description;

}