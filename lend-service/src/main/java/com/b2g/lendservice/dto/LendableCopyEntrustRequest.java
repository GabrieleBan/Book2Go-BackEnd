package com.b2g.lendservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;
@Data
public class LendableCopyEntrustRequest {
    @NotNull
    UUID userId;
    @NotNull
    Integer copyNumber;
    @NotNull
    UUID libraryId;
}
