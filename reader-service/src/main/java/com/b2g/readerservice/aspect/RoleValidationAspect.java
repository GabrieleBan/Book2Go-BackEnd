package com.b2g.readerservice.aspect;

import com.b2g.readerservice.annotation.*;
import com.b2g.readerservice.service.*;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RoleValidationAspect {


    private final remoteJwtService remoteJwtService;

    @Around("@annotation(requireRole)")
    public Object validateRole(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or missing authentication");
        }

        // Principal contiene le Claims messe dal tuo filtro
        Claims claims = (Claims) auth.getPrincipal();

        // Qui non chiami più remote
        List<String> userRoles = remoteJwtService.extractRoles(claims);

        String[] requiredRoles = requireRole.value();
        boolean hasRequiredRole =
                checkUserRoles(userRoles, requiredRoles, requireRole.requireAll());

        if (!hasRequiredRole) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Insufficient privileges. Required: " + Arrays.toString(requiredRoles));
        }

        return joinPoint.proceed();


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
