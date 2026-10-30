package com.cocktail.cocktailproject.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * SpringDocConfig - OpenAPI/Swagger configuration with Keycloak Authentication
 * 
 * Swagger UI: http://localhost:8080/swagger-ui.html
 * OpenAPI JSON: http://localhost:8080/v3/api-docs
 * 
 * Per autenticarsi:
 * 1. Call the POST /api/auth/login endpoint with username and password
 * 2. Copy the access_token from the response
 * 3. Click "Authorize" in Swagger UI
 * 4. Paste the token (without the "Bearer" prefix)
 * 5. The token will be automatically included in all protected requests
 */
@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
            .info(new Info()
                .title("Cocktail API")
                .version("1.0.0")
                .description("API REST per la gestione di cocktail, ingredienti e preparazioni. " +
                           "Supporta operazioni CRUD complete e ricerca per nome.\n\n" +
                           "---\n\n" +
                           "## GUIDA COMPLETA: Registrazione e Login\n\n" +
                           "### Opzione 1: Creare un Nuovo Account (Registrazione)\n\n" +
                           "**Step 1: Vai all'endpoint di Registrazione**\n" +
                           "- Scorri fino al controller `AuthController`\n" +
                           "- Trova l'endpoint `POST /api/auth/register`\n" +
                           "- Clicca 'Try it out'\n\n" +
                           "**Step 2: Compila il modulo di registrazione**\n" +
                           "Inserisci un corpo JSON come questo:\n" +
                           "```json\n" +
                           "{\n" +
                           "  \"username\": \"marco_rossi\",\n" +
                           "  \"email\": \"marco@example.com\",\n" +
                           "  \"password\": \"MySecurePassword123!\",\n" +
                           "  \"firstName\": \"Marco\",\n" +
                           "  \"lastName\": \"Rossi\"\n" +
                           "}\n" +
                           "```\n" +
                           "**Step 3: Clicca 'Execute'**\n" +
                           "- Se il registro ha successo, riceverai un messaggio di conferma\n" +
                           "- Il tuo nuovo account sarà creato e pronto per il login\n\n" +
                           "---\n\n" +
                           "### Opzione 2: Usare Utenti di Test (Consigliato per iniziare)\n\n" +
                           "Se preferisci iniziare subito, usa questi account:\n\n" +
                           "| Username | Password | Ruolo | Accesso |\n" +
                           "|----------|----------|-------|----------|\n" +
                           "| `user` | 'user' | USER | Sola lettura |\n" +
                           "| `soldier` | vedi sotto | ADMIN | Accesso completo |\n\n" +
                           "Contatta il sommo maestro Alex per la password di test admin.\n\n" +
                           "---\n\n" +
                           "## COME ESEGUIRE IL LOGIN\n\n" +
                           "**Step 1: Accedi all'endpoint di Login**\n" +
                           "- Scorri fino al controller `AuthController`\n" +
                           "- Trova l'endpoint `POST /api/auth/login`\n" +
                           "- Clicca 'Try it out'\n\n" +
                           "**Step 2: Inserisci le tue credenziali**\n" +
                           "Utilizza il corpo JSON con il tuo username e password:\n" +
                           "```json\n" +
                           "{\n" +
                           "  \"username\": \"marco_rossi\",\n" +
                           "  \"password\": \"MySecurePassword123!\"\n" +
                           "}\n" +
                           "```\n\n" +
                           "**Step 3: Clicca 'Execute'**\n" +
                           "Riceverai una risposta simile a questa:\n" +
                           "```json\n" +
                           "{\n" +
                           "  \"access_token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\",\n" +
                           "  \"token_type\": \"Bearer\",\n" +
                           "  \"expires_in\": 1200,\n" +
                           "  \"username\": \"marco_rossi\"\n" +
                           "}\n" +
                           "```\n\n" +
                           "**Step 4: Copia l'access_token**\n" +
                           "- Seleziona il valore di `access_token` dalla risposta\n" +
                           "- Copialo negli appunti\n\n" +
                           "---\n\n" +
                           "## COME AUTORIZZARE LE TUE RICHIESTE IN SWAGGER\n\n" +
                           "**Step 1: Clicca il pulsante 'Authorize'**\n" +
                           "- Cerca il pulsante 'Authorize' con l'icona di lucchetto in alto a destra\n" +
                           "- Clicca su di esso\n\n" +
                           "**Step 2: Incolla il token**\n" +
                           "- Nel campo 'Value' della sezione 'bearerAuth'\n" +
                           "- Incolla il token che hai copiato\n" +
                           "- **Nota:** Non serve aggiungere 'Bearer ' come prefisso, verrà aggiunto automaticamente\n\n" +
                           "**Step 3: Clicca 'Authorize' e poi 'Close'**\n" +
                           "- Il token è ora memorizzato per tutte le tue richieste\n\n" +
                           "---\n\n" +
                           "## UTILIZZA LE API PROTETTE\n\n" +
                           "Ora puoi:\n" +
                           "- Visualizzare tutti i cocktail disponibili\n" +
                           "- Creare, modificare e eliminare cocktail (se sei admin)\n" +
                           "- Gestire i tuoi preferiti\n" +
                           "- Visualizzare ingredienti e preparazioni\n\n" +
                           "Il token JWT verrà incluso automaticamente nell'header:\n" +
                           "```\n" +
                           "Authorization: Bearer {token}\n" +
                           "```\n\n" +
                           "**Il token scade dopo 20 min. Esegui di nuovo il login quando scade.**")) 
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080")
                    .description("Server di sviluppo locale")
            ))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Inserisci il JWT token ottenuto dall'endpoint /api/auth/login. " +
                               "Non serve aggiungere 'Bearer ' come prefisso, viene aggiunto automaticamente.")))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    }
}
