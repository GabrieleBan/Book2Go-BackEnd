package com.b2g.rentalservice.dto;

import com.b2g.rentalservice.model.RentalBookFormat;
import com.b2g.rentalservice.model.RentalOption;
import lombok.Data;


public record FormatOptionLink(
        RentalBookFormat format,
        RentalOption option
) {}