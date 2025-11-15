package com.example.ragchatstorage.config;

import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * Custom authentication token for API Key authentication.
 * This is used by Spring Security's authentication mechanism.
 */
public class ApiKeyAuthentication extends AbstractAuthenticationToken {

    private final String apiKey;

    public ApiKeyAuthentication(String apiKey) {
        super(null);
        this.apiKey = apiKey;
        setAuthenticated(false);
    }

    public ApiKeyAuthentication(String apiKey, boolean authenticated) {
        super(null);
        this.apiKey = apiKey;
        setAuthenticated(authenticated);
    }

    @Override
    public Object getCredentials() {
        return apiKey;
    }

    @Override
    public Object getPrincipal() {
        return apiKey;
    }
}

