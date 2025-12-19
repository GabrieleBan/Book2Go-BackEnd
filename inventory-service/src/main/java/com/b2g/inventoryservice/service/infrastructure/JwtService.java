package com.b2g.inventoryservice.service.infrastructure;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {



    public Claims validateToken(String token) {
        return null;
    }

    /**
     * Extract roles from JWT claims.
     * Supports both "roles", "role" and "authorities" claim names.
     *
     * @param claims JWT claims
     * @return List of role names
     */
    public List<String> extractRoles(Claims claims) {
        List<String> roles = new ArrayList<>();

        // Try to get roles from "roles" claim first (plural)
        Object rolesClaim = claims.get("roles");
        if (rolesClaim != null) {
            if (rolesClaim instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> rolesList = (List<String>) rolesClaim;
                roles.addAll(rolesList);
            } else if (rolesClaim instanceof String) {
                // Single role as string
                roles.add((String) rolesClaim);
            }
        }

        // Also try "role" claim (singular)
        Object roleClaim = claims.get("role");
        if (roleClaim != null) {
            if (roleClaim instanceof String) {
                roles.add((String) roleClaim);
            } else if (roleClaim instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> rolesList = (List<String>) roleClaim;
                roles.addAll(rolesList);
            }
        }

        // Also try "authorities" claim (common in Spring Security)
        Object authoritiesClaim = claims.get("authorities");
        if (authoritiesClaim != null) {
            if (authoritiesClaim instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> authoritiesList = (List<Object>) authoritiesClaim;
                for (Object authority : authoritiesList) {
                    if (authority instanceof String) {
                        roles.add((String) authority);
                    } else if (authority instanceof Map) {
                        // Handle Spring Security GrantedAuthority format: {"authority": "ROLE_ADMIN"}
                        @SuppressWarnings("unchecked")
                        Map<String, Object> authorityMap = (Map<String, Object>) authority;
                        Object authorityValue = authorityMap.get("authority");
                        if (authorityValue instanceof String) {
                            roles.add((String) authorityValue);
                        }
                    }
                }
            }
        }

        return roles;
    }
}
