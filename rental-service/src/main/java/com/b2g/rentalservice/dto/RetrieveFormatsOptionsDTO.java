package com.b2g.rentalservice.dto;

import com.b2g.rentalservice.model.FormatType;
import com.b2g.rentalservice.model.RentalOption;
import lombok.Builder;
import lombok.Data;

import java.util.Set;
import java.util.UUID;
@Data
@Builder
public class RetrieveFormatsOptionsDTO {
    private UUID rentalBookFormatId;
    private UUID parentBook;
    private FormatType format;

    private Set<RentalOption> options;


}
