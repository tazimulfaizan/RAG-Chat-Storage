package com.example.ragchatstorage.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/**
 * Spring Security Authentication Provider for API Key authentication.
 * Validates API keys against the configured value from application.yml.
 */
@Component
@Slf4j
public class ApiKeyAuthenticationProvider implements AuthenticationProvider {

    @Value("${security.api-key}")
    private String configuredApiKey;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String providedKey = (String) authentication.getPrincipal();

        if (providedKey == null || !configuredApiKey.equals(providedKey)) {
            log.warn("Invalid API key provided");
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

