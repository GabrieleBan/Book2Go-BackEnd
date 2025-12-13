package com.b2g.commons;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;
@Data
public class LendRequest {
    @NotNull
    private UUID formatId;
    @NotNull
    private UUID libraryId;
}
