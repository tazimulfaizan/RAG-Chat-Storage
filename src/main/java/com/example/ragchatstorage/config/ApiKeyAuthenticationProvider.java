package com.example.ragchatstorage.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Spring Security Authentication Provider for API Key authentication.
 * Supports multiple API keys for different clients (frontend, mobile, backend).
 * Validates API keys against configured values from application.yml.
 *
 * Configuration: security.api-keys (comma-separated list)
 */
@Component
@Slf4j
public class ApiKeyAuthenticationProvider implements AuthenticationProvider {

    private final Set<String> validApiKeys;

    public ApiKeyAuthenticationProvider(@Value("${security.api-keys:changeme}") String apiKeys) {
        // Parse comma-separated API keys into a Set for O(1) lookup
        this.validApiKeys = new HashSet<>(Arrays.asList(apiKeys.split(",")));
        this.validApiKeys.removeIf(String::isBlank); // Remove empty strings

        log.info("API Key Authentication initialized with {} valid key(s)", validApiKeys.size());
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String providedKey = (String) authentication.getPrincipal();

        if (providedKey == null || providedKey.isBlank()) {
            log.warn("API key is missing or empty");
            throw new BadCredentialsException("API key is required");
        }

        // Check if provided key exists in valid keys set
        if (!validApiKeys.contains(providedKey.trim())) {
            log.warn("Invalid API key attempt: {}***",
                    providedKey.substring(0, Math.min(4, providedKey.length())));
            throw new BadCredentialsException("Invalid API key");
        }

        log.debug("API key authenticated successfully");
        return new ApiKeyAuthentication(providedKey, true);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return ApiKeyAuthentication.class.isAssignableFrom(authentication);
    }
}

