package com.b2g.authservice.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.UUID;

@Data
@Builder
@AllArgsConstructor
public class TokenResponse {
    @NotEmpty
    private String accessToken;
    @NotEmpty
    @UUID
    private String refreshToken;
}