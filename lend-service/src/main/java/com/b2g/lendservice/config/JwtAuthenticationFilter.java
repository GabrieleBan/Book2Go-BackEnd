package com.b2g.lendservice.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.b2g.lendservice.service.infrastructure.remoteJwtService;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final remoteJwtService remoteJwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        System.out.println("Internal Authorization header: " + authHeader);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7).trim();

        if (jwt.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {

        Claims claims = remoteJwtService.remoteValidateToken(jwt);
        String userId = claims.getSubject();
        System.out.println(userId);
        System.out.println(claims);
        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(claims, jwt, Collections.emptyList());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (SessionAuthenticationException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            String errorJson = String.format("{\"error\": \"%s\"}", e.getMessage().replace("\"", "'"));
            response.getWriter().write(errorJson);
            response.getWriter().flush();
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        List<String> publicPaths = List.of(
        );
        System.out.println(path);
        boolean b = publicPaths.stream().anyMatch(publicPath -> pathMatches(publicPath, path));
        System.out.println(b);
        return b;
    }



    private boolean pathMatches(String pattern, String path) {

        // Case: "/auth/**" → deve avere QUALCOSA dopo la slash
        if (pattern.endsWith("/**")) {
            String base = pattern.substring(0, pattern.length() - 3); // "/auth"
            if (!path.startsWith(base)) return false;

            String remaining = path.substring(base.length()); // es. "/xyz", "/" oppure ""
            return remaining.length() > 1;                    // deve essere più di "/"
        }

        // Case: "/auth/" → matcha solo il prefisso esatto
        if (pattern.endsWith("/")) {
            return path.startsWith(pattern);
        }

        // Exact match: "/login"
        return path.equals(pattern);
    }
}