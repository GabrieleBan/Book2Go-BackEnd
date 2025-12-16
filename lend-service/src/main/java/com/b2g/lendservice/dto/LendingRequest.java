package com.b2g.lendservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;
public record LendingRequest (
        @NotNull UUID lendableBookId,
        UUID libraryId){

}
