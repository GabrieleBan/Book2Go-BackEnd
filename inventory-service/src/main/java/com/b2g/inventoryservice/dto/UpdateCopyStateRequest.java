package com.b2g.inventoryservice.dto;

import com.b2g.inventoryservice.model.CopyUseState;
import jakarta.validation.constraints.NotNull;

public record UpdateCopyStateRequest(@NotNull CopyUseState state) {}