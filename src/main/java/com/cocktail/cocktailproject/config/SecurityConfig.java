package com.cocktail.cocktailproject.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;

/**
 * SecurityConfig - Two separate filter chains:
 * 
 * 1. publicSecurityFilterChain (Order 1) - No JWT authentication:
 *    - /api/auth/** (login, registration)
 *    - Swagger UI
 * 
 * 2. protectedSecurityFilterChain (Order 2) - JWT authentication required:
 *    - GET /api/cocktails (public)
 *    - POST/PUT/DELETE /api/cocktails -> Admin only
 */@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(auth -> auth

                // PUBLIC ENDPOINTS
                .requestMatchers(
                        "/api/auth/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**"
                ).permitAll()

                // PUBLIC GET cocktails
                .requestMatchers(HttpMethod.GET, "/api/cocktails/**").permitAll()

                // PROTECTED POST cocktails (authenticated users)
                .requestMatchers(HttpMethod.POST, "/api/cocktails/**").authenticated()

                // PROTECTED ENDPOINTS (Admin)
                .requestMatchers(HttpMethod.PUT, "/api/cocktails/**").hasRole("SOLDIER")
                .requestMatchers(HttpMethod.DELETE, "/api/cocktails/**").hasRole("SOLDIER")

                // PUBLIC GET ingredients
                .requestMatchers(HttpMethod.GET, "/api/ingredients/**").permitAll()
                // PROTECTED DELETE ingredients (authenticated users)
                .requestMatchers(HttpMethod.DELETE, "/api/ingredients/**").hasRole("SOLDIER")

                // Favorites -> authenticated users
                .requestMatchers("/api/favoriti/**").authenticated()

                // Everything else requires authentication
                .anyRequest().authenticated()
            )

            // JWT (Keycloak)
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );

        return http.build();
    }

    // Extract roles from the JWT (Keycloak)
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        // Custom converter to extract roles from realm_access.roles
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            var realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                var roles = (java.util.Collection<String>) realmAccess.get("roles");
                return roles.stream()
                        .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role))
                        .collect(java.util.stream.Collectors.toList());
            }
            return java.util.Collections.emptyList();
        });

        return converter;
    }

    // ObjectMapper bean - configured to ignore unknown fields
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(
            com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            false
        );
        return mapper;
    }

    // RestTemplate bean - used for HTTP calls to Keycloak
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
