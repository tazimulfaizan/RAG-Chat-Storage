package com.example.ragchatstorage.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * Spring Security Configuration using native Spring Security mechanisms.
 * Also includes CORS configuration.
 *
 * Security Rules:
 * - All /api/** endpoints require API key authentication
 * - Public endpoints: /actuator/**, /swagger-ui/**, /v3/api-docs/**
 * - API key authentication via Spring Security's AuthenticationManager
 *
 * Configuration loaded from application.yml (security.*, app.cors.*)
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ApiKeyAuthenticationProvider apiKeyAuthenticationProvider;
    private final ApiKeyAuthenticationEntryFilter apiKeyAuthenticationEntryFilter;

    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods:GET,POST,PATCH,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${app.cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${app.cors.max-age:3600}")
    private long maxAge;

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(apiKeyAuthenticationProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for API (stateless authentication)
            .csrf(AbstractHttpConfigurer::disable)

            // Enable CORS with default configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Session management - stateless (no sessions)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - no authentication required
                .requestMatchers(
                    "/actuator/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()

                // Protected endpoints - require authentication
                .requestMatchers("/api/**").authenticated()

                // All other endpoints - deny by default
                .anyRequest().denyAll()
            )

            // Add API key authentication filter
            .addFilterBefore(apiKeyAuthenticationEntryFilter, UsernamePasswordAuthenticationFilter.class)

            // Disable default authentication mechanisms
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Parse allowed origins
        String[] origins = allowedOrigins.split(",");
        configuration.setAllowedOrigins(Arrays.asList(origins));

        // Parse allowed methods
        String[] methods = allowedMethods.split(",");
        configuration.setAllowedMethods(Arrays.asList(methods));

        // Set allowed headers
        if (allowedHeaders.equals("*")) {
            configuration.addAllowedHeader("*");
        } else {
            String[] headers = allowedHeaders.split(",");
            configuration.setAllowedHeaders(Arrays.asList(headers));
        }

        // Allow credentials if not wildcard origin
        configuration.setAllowCredentials(!allowedOrigins.equals("*"));

        // Set max age
        configuration.setMaxAge(maxAge);

        // Apply to all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * CORS configuration bean for WebMvc
     * Backup CORS configuration
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                String[] origins = allowedOrigins.split(",");
                String[] methods = allowedMethods.split(",");
                boolean allowCredentials = !allowedOrigins.equals("*");

                registry.addMapping("/**")
                        .allowedOrigins(origins)
                        .allowedMethods(methods)
                        .allowedHeaders(allowedHeaders)
                        .allowCredentials(allowCredentials)
                        .maxAge(maxAge);
            }
        };
    }
}

