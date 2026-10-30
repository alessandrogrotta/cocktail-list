package com.cocktail.cocktailproject.service;

import com.cocktail.cocktailproject.dto.LoginRequestDTO;
import com.cocktail.cocktailproject.dto.TokenResponseDTO;
import com.cocktail.cocktailproject.dto.UserRegistrationDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

/**
 * KeycloakUserService - Keycloak integration for authentication and user management
 * 
 * Supported operations:
 * 1. ROPC login (Resource Owner Password Credentials)
 *    - The user submits a username/password
 *    - Keycloak returns a JWT access_token
 *    - The token authenticates protected API calls
 * 
 * 2. User registration
 *    - Creates a new user on Keycloak
 *    - Sets the password
 *    - Assigns the default role (USER)
 *    - Verifies username and email uniqueness
 * 
 * 3. Role management
 *    - Assigns realm roles to users
 *    - Roles determine permissions (e.g. SOLDIER for write operations)
 * 
 * Configuration:
 * - Keycloak admin credentials are in application.properties
 * - The target realm is configurable
 * - Supports clients with or without a client_secret
 */
@Service
public class KeycloakUserService {

    @Value("${keycloak.admin.server-url}")
    private String serverUrl;

    @Value("${keycloak.admin.realm}")
    private String adminRealm;

    @Value("${keycloak.admin.client-id}")
    private String clientId;

    @Value("${keycloak.admin.username}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id:swagger-ui}")
    private String loginClientId;

    @Value("${keycloak.client-secret:}")
    private String clientSecret;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(KeycloakUserService.class);
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    
    // Constructor injection for ObjectMapper and RestTemplate
    public KeycloakUserService(ObjectMapper objectMapper, RestTemplate restTemplate) {
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    /**
     * Logs in using Resource Owner Password Credentials (ROPC).
     * 
     * @param loginRequest user's username and password
     * @return token response with access_token
     * @throws RuntimeException if credentials are invalid or login fails
     */
    public TokenResponseDTO login(LoginRequestDTO loginRequest) {
        String tokenUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", loginClientId);
        body.add("username", loginRequest.getUsername());
        body.add("password", loginRequest.getPassword());
        body.add("scope", "openid profile email roles");

        if (clientSecret != null && !clientSecret.isEmpty()) {
            body.add("client_secret", clientSecret);
        }

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            MediaType contentType = response.getHeaders().getContentType();
            String responseBody = response.getBody();

            if (responseBody == null || responseBody.isBlank()) {
                throw new RuntimeException("Risposta vuota dal server di autenticazione.");
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Keycloak /token status={} contentType={} bodyPreview={}...",
                        response.getStatusCode(),
                        contentType,
                        responseBody.substring(0, Math.min(responseBody.length(), 200))
                );
            }

            if (contentType == null || MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
                try {
                    return objectMapper.readValue(responseBody, TokenResponseDTO.class);
                } catch (JsonProcessingException ex) {
                    try {
                        JsonNode node = objectMapper.readTree(responseBody);
                        if (node.has("error_description")) {
                            throw new RuntimeException("Errore durante il login: " + node.get("error_description").asText());
                        }
                        if (node.has("error")) {
                            throw new RuntimeException("Errore durante il login: " + node.get("error").asText());
                        }
                    } catch (Exception ignore) {}
                    throw new RuntimeException("Risposta non valida dal server di autenticazione (JSON non parsabile).");
                }
            }

            throw new RuntimeException("Risposta inattesa dal server di autenticazione: " +
                    (contentType != null ? contentType.toString() : "contenuto sconosciuto") +
                    ". Dettagli: " + responseBody.substring(0, Math.min(responseBody.length(), 300))
            );

        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            MediaType ct = e.getResponseHeaders() != null ? e.getResponseHeaders().getContentType() : null;

            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                String msg = "Credenziali non valide. Username o password errati.";
                if (responseBody != null && !responseBody.isBlank()) {
                    try {
                        JsonNode node = objectMapper.readTree(responseBody);
                        if (node.has("error_description")) {
                            msg = node.get("error_description").asText();
                        }
                    } catch (Exception ignore) {}
                }
                throw new RuntimeException(msg);
            }

            if (responseBody != null && !responseBody.isBlank()) {
                if (ct != null && MediaType.APPLICATION_JSON.isCompatibleWith(ct)) {
                    try {
                        JsonNode node = objectMapper.readTree(responseBody);
                        if (node.has("error_description")) {
                            throw new RuntimeException("Errore durante il login: " + node.get("error_description").asText(), e);
                        }
                        if (node.has("error")) {
                            throw new RuntimeException("Errore durante il login: " + node.get("error").asText(), e);
                        }
                    } catch (Exception ignore) {}
                }
                throw new RuntimeException("Errore durante il login: " + responseBody.substring(0, Math.min(responseBody.length(), 300)), e);
            }

            throw new RuntimeException("Errore durante il login: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Errore durante il login: " + e.getMessage(), e);
        }
    }

    /**
     * Creates an admin connection to Keycloak.
     */
    private Keycloak getKeycloakInstance() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(adminRealm)
                .clientId(clientId)
                .username(adminUsername)
                .password(adminPassword)
                .build();
    }

    /**
     * Checks whether a user already exists (by username or email).
     * 
     * @param username username to check
     * @param email email to check
     * @return true if the user exists, false otherwise
     */
    public boolean userExists(String username, String email) {
        try (Keycloak keycloak = getKeycloakInstance()) {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            // Search by username
            List<UserRepresentation> usersByUsername = usersResource.search(username, true);
            if (!usersByUsername.isEmpty()) {
                return true;
            }

            // Search by email
            List<UserRepresentation> usersByEmail = usersResource.search(null, null, null, email, 0, 1);
            return !usersByEmail.isEmpty();
        } catch (Exception e) {
            throw new RuntimeException("Errore durante la verifica dell'utente: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a new user on Keycloak.
     * 
     * @param registrationDTO data for the user to create
     * @return success message
     * @throws RuntimeException if the user already exists or creation fails
     */
    public String createUser(UserRegistrationDTO registrationDTO) {
        // Check whether the user already exists
        if (userExists(registrationDTO.getUsername(), registrationDTO.getEmail())) {
            throw new RuntimeException("Username o email già in uso");
        }

        try (Keycloak keycloak = getKeycloakInstance()) {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            // Create the user representation
            UserRepresentation user = new UserRepresentation();
            user.setUsername(registrationDTO.getUsername());
            user.setEmail(registrationDTO.getEmail());
            user.setFirstName(registrationDTO.getFirstName());
            user.setLastName(registrationDTO.getLastName());
            user.setEnabled(true);
            user.setEmailVerified(false); // The user must verify the email (if configured)

            // Create the user
            Response response = usersResource.create(user);

            if (response.getStatus() != 201) {
                String errorMessage = response.readEntity(String.class);
                throw new RuntimeException("Errore durante la creazione dell'utente: " + errorMessage);
            }

            // Get the ID of the newly created user
            String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

            // Set the password
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(registrationDTO.getPassword());
            credential.setTemporary(false); // Does not require a password change on first login

            usersResource.get(userId).resetPassword(credential);

            // Assign the default "User" role
            assignRoleToUser(keycloak, realmResource, userId, "USER");

            response.close();
            return "Utente creato con successo";

        } catch (Exception e) {
            throw new RuntimeException("Errore durante la creazione dell'utente: " + e.getMessage(), e);
        }
    }

    /**
     * Assigns a realm role to a user.
     * 
     * @param keycloak Keycloak instance
     * @param realmResource realm resource
     * @param userId user ID
     * @param roleName name of the role to assign
     */
    private void assignRoleToUser(Keycloak keycloak, RealmResource realmResource, String userId, String roleName) {
        try {
            // Get the realm role
            RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();

            // Assign the role to the user
            realmResource.users().get(userId).roles().realmLevel().add(Collections.singletonList(role));

        } catch (Exception e) {
            // Log the error without blocking user creation
            System.err.println("Attenzione: impossibile assegnare il ruolo '" + roleName + "' all'utente: " + e.getMessage());
        }
    }
}
