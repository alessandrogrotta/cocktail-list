package com.cocktail.cocktailproject.controller;

import com.cocktail.cocktailproject.dto.LoginRequestDTO;
import com.cocktail.cocktailproject.dto.TokenResponseDTO;
import com.cocktail.cocktailproject.dto.UserRegistrationDTO;
import com.cocktail.cocktailproject.service.KeycloakUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AuthController - Authentication and registration endpoints
 * 
 * Handles:
 * - User login (Resource Owner Password Credentials)
 * - New user registration on Keycloak
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@Tag(name = "Authentication", description = "API per autenticazione e registrazione utenti")
public class AuthController {

    @Autowired
    private KeycloakUserService keycloakUserService;

    /**
     * POST /api/auth/login - Logs in and obtains an access token
     * 
     * Public endpoint (no authentication required)
     * Uses the Resource Owner Password Credentials (ROPC) flow
     */
    @Operation(
        summary = "Login utente",
        description = "Effettua il login usando username e password. Restituisce un access token JWT valido " +
                     "che può essere usato per autenticare le chiamate API protette. " +
                     "Il token va incluso nell'header Authorization: Bearer {access_token}"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login effettuato con successo, token restituito",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TokenResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dati non validi (username o password mancanti)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Credenziali non valide (username o password errati)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Errore interno del server",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Credenziali di login (username e password)",
                required = true,
                content = @Content(schema = @Schema(implementation = LoginRequestDTO.class))
            )
            @Valid @RequestBody LoginRequestDTO loginRequest,
            BindingResult bindingResult) {

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getAllErrors()
                    .stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Errori di validazione");
            errorResponse.put("errors", errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        try {
            // Log in through Keycloak
            TokenResponseDTO tokenResponse = keycloakUserService.login(loginRequest);

            // Return the token as JSON with "access_token" and "expires_in" fields
            return ResponseEntity.ok(tokenResponse);

        } catch (RuntimeException e) {
            // Handle authentication errors
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());

            if (e.getMessage().contains("Credenziali non valide")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * POST /api/auth/register - Registers a new user
     * 
     * Public endpoint (no authentication required)
     * Creates a new user on Keycloak with the default "user" role
     */
    @Operation(
        summary = "Registra un nuovo utente",
        description = "Crea un nuovo utente nel sistema Keycloak. L'utente riceverà automaticamente il ruolo 'user'. " +
                     "Username ed email devono essere univoci. La password e la conferma password devono coincidere."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Utente creato con successo",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dati non validi o password non coincidono",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Username o email già esistente",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Errore interno del server",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Dati dell'utente da registrare",
                required = true,
                content = @Content(schema = @Schema(implementation = UserRegistrationDTO.class))
            )
            @Valid @RequestBody UserRegistrationDTO registrationDTO,
            BindingResult bindingResult) {

        Map<String, Object> response = new HashMap<>();

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getAllErrors()
                    .stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));

            response.put("success", false);
            response.put("message", "Errori di validazione");
            response.put("errors", errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Verify that password and confirmPassword match
        if (!registrationDTO.getPassword().equals(registrationDTO.getConfirmPassword())) {
            response.put("success", false);
            response.put("message", "Password e conferma password non coincidono");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            // Create the user on Keycloak
            String message = keycloakUserService.createUser(registrationDTO);

            response.put("success", true);
            response.put("message", message);
            response.put("username", registrationDTO.getUsername());
            response.put("email", registrationDTO.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            // Handle specific errors (e.g. user already exists)
            if (e.getMessage().contains("già in uso")) {
                response.put("success", false);
                response.put("message", e.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            // Generic error
            response.put("success", false);
            response.put("message", "Errore durante la registrazione: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
