package com.b2g.commons;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
@Data
@Getter
@Setter
public class AuthValidationMessage  {
    private String authToken;
    private String refreshToken;
    private Map<String, String> sendBack;
    private Map<String, String> toAdd;
    private String error;
}

