package com.b2g.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginRequest {
    @NotEmpty
    @Size(min = 5, max = 20, message = "Username must be between 5 and 20 characters")
    private String username;
    @NotEmpty
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;
}