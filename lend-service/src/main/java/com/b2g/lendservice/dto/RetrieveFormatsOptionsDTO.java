package com.b2g.lendservice.dto;

import com.b2g.commons.FormatType;
import com.b2g.lendservice.model.LendingOption;
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

    private Set<LendingOption> options;


}
