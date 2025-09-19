package com.b2g.catalogservice.aspect;

import com.b2g.catalogservice.annotation.RequireRole;
import com.b2g.catalogservice.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.List;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RoleValidationAspect {

    private final JwtService jwtService;

    @Around("@annotation(requireRole)")
    public Object validateRole(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        try {
            // Get the current HTTP request
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();

            // Extract JWT token from Authorization header
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header for role-protected endpoint");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Missing or invalid Authorization header");
            }

            String jwt = authHeader.substring(7);
            if (jwt.trim().isEmpty()) {
                log.warn("Empty JWT token for role-protected endpoint");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid token");
            }

            // Validate JWT and extract claims
            Claims claims = jwtService.validateToken(jwt);

            // Extract roles from JWT claims
            List<String> userRoles = jwtService.extractRoles(claims);

            // Check if user has required roles
            String[] requiredRoles = requireRole.value();
            boolean hasRequiredRole = checkUserRoles(userRoles, requiredRoles, requireRole.requireAll());

            if (!hasRequiredRole) {
                log.warn("User with roles {} does not have required roles {} for endpoint {}",
                        userRoles, Arrays.toString(requiredRoles), request.getRequestURI());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Insufficient privileges. Required role(s): " + Arrays.toString(requiredRoles));
            }

            // User has required role(s), proceed with method execution
            log.debug("User with roles {} authorized for endpoint {}", userRoles, request.getRequestURI());
            return joinPoint.proceed();

        } catch (Exception e) {
            log.error("Error during role validation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Token validation failed: " + e.getMessage());
        }
    }

    private boolean checkUserRoles(List<String> userRoles, String[] requiredRoles, boolean requireAll) {
        if (userRoles == null || userRoles.isEmpty()) {
            return false;
        }

        if (requireAll) {
            // User must have ALL required roles
            return userRoles.containsAll(Arrays.asList(requiredRoles));
        } else {
            // User must have at least ONE of the required roles
            return Arrays.stream(requiredRoles)
                    .anyMatch(userRoles::contains);
        }
    }
}
