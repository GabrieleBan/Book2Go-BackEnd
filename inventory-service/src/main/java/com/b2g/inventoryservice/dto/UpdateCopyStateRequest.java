package com.b2g.inventoryservice.dto;

import com.b2g.inventoryservice.model.valueObjects.AvailabilityState;
import jakarta.validation.constraints.NotNull;

public record UpdateCopyStateRequest(@NotNull AvailabilityState state) {}