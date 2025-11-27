package com.b2g.rentalservice.dto;

import com.b2g.rentalservice.model.RentalOption;

import java.util.UUID;

public class RentalCreationRequestForFormatDTO {
    private UUID book_format_id;
    private RentalOption rental_option_to_create;
}
