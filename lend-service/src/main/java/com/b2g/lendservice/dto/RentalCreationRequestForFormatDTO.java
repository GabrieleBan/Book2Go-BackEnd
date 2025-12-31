package com.b2g.lendservice.dto;

import com.b2g.lendservice.model.vo.LendingOption;

import java.util.UUID;

public class RentalCreationRequestForFormatDTO {
    private UUID book_format_id;
    private LendingOption rental_option_to_create;
}
