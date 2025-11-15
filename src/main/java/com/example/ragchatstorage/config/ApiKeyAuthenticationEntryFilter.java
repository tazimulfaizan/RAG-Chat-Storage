package com.example.ragchatstorage.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter to extract API key from request header and authenticate using Spring Security.
 * This integrates with Spring Security's AuthenticationManager.
 * Uses @Lazy to avoid circular dependency with SecurityConfig.
 */
@Component
public class ApiKeyAuthenticationEntryFilter extends OncePerRequestFilter {

    private final AuthenticationManager authenticationManager;

    @Value("${security.api-key-header:X-API-KEY}")
    private String apiKeyHeader;

    public ApiKeyAuthenticationEntryFilter(@Lazy AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String apiKey = request.getHeader(apiKeyHeader);

        if (apiKey != null) {
            try {
                ApiKeyAuthentication authentication = new ApiKeyAuthentication(apiKey);
                ApiKeyAuthentication authenticated = (ApiKeyAuthentication) authenticationManager.authenticate(authentication);
                SecurityContextHolder.getContext().setAuthentication(authenticated);
            } catch (Exception e) {
                // Authentication failed - Spring Security will handle 401
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}

