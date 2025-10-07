package com.b2g.orderservice.controller;

import com.b2g.orderservice.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final JwtService jwtService;

    //Hello World Mapping for testing
    @GetMapping({"", "/"})
    public Map<String, Object> hello(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello from Order Service!");

        // Estrai il JWT dall'header Authorization
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);

            try {
                Claims claims = jwtService.validateToken(jwt);

                // Aggiungi tutti i claims del JWT alla risposta
                Map<String, Object> jwtContent = new HashMap<>();
                jwtContent.put("subject", claims.getSubject());
                jwtContent.put("issuer", claims.getIssuer());
                jwtContent.put("audience", claims.getAudience());
                jwtContent.put("expiration", claims.getExpiration());
                jwtContent.put("issuedAt", claims.getIssuedAt());
                jwtContent.put("roles", jwtService.extractRoles(claims));

                // Aggiungi tutti gli altri claims personalizzati
                claims.forEach((key, value) -> {
                    if (!key.equals("sub") && !key.equals("iss") && !key.equals("aud")
                        && !key.equals("exp") && !key.equals("iat")) {
                        jwtContent.put(key, value);
                    }
                });

                response.put("jwt", jwtContent);
            } catch (Exception e) {
                response.put("jwtError", "Invalid or expired token: " + e.getMessage());
            }
        } else {
            response.put("jwtError", "No JWT token found in Authorization header");
        }

        return response;
    }
}