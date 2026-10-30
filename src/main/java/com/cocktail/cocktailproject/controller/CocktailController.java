package com.cocktail.cocktailproject.controller;

import com.cocktail.cocktailproject.dto.CocktailDTO;
import com.cocktail.cocktailproject.dto.CreateCocktailRequestDTO;
import com.cocktail.cocktailproject.dto.IngredientiDTO;
import com.cocktail.cocktailproject.service.CocktailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * CocktailController - REST endpoints for managing cocktails.
 * Contains CRUD operations and paginated search.
 */
@RestController
@RequestMapping("/api/cocktails")
@CrossOrigin(origins = "*")
@Tag(name = "Cocktail", description = "API per la gestione dei cocktail")
public class CocktailController {

    @Autowired
    private CocktailService cocktailService;

    /**
     * GET /api/cocktails - Gets all cocktails with pagination
     * @param page page number (default 0)
     * @param size number of cocktails per page (default 10)
     */
    @Operation(summary = "Ottieni tutti i cocktail", description = "Restituisce una lista paginata di cocktail disponibili nel sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista paginata di cocktail recuperata con successo")
    })
    @GetMapping
    public ResponseEntity<Page<CocktailDTO>> getAllCocktails(
            @Parameter(description = "Numero della pagina (base 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Numero di cocktail per pagina (default 10)")
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CocktailDTO> cocktails = cocktailService.getAllCocktails(pageable);
        return ResponseEntity.ok(cocktails);
    }

    /**
     * GET /api/cocktails/{id} - Gets a cocktail by ID
     */
    @Operation(summary = "Ottieni cocktail per ID", description = "Restituisce un cocktail specifico basato sul suo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cocktail trovato e restituito"),
            @ApiResponse(responseCode = "404", description = "Cocktail non trovato")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CocktailDTO> getCocktailById(
            @Parameter(description = "ID del cocktail da recuperare")
            @PathVariable Long id) {
        Optional<CocktailDTO> cocktail = cocktailService.getCocktailById(id);
        return cocktail.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /api/cocktails/search?nome=xxx - Searches cocktails by name with pagination
     * @param nome name to search for
     * @param page page number (default 0)
     * @param size number of cocktails per page (default 10)
     */
    @Operation(summary = "Cerca cocktail per nome", description = "Restituisce una lista paginata di cocktail che corrispondono al nome ricercato")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ricerca completata, lista paginata di cocktail restituita")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<CocktailDTO>> searchByName(
            @Parameter(description = "Nome del cocktail da cercare")
            @RequestParam String nome,
            @Parameter(description = "Numero della pagina (base 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Numero di cocktail per pagina (default 10)")
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CocktailDTO> cocktails = cocktailService.searchByName(nome, pageable);
        return ResponseEntity.ok(cocktails);
    }

    /**
     * POST /api/cocktails - Creates a new cocktail with ingredients and preparation
     * 
     * Example request:
     * {
     *   "nome": "Margarita",
     *   "descrizione": "Mexican cocktail",
     *   "tempoPreparazioneMinutes": 5,
     *   "ingredienti": [
     *     { "nome": "tequila", "quantita": 2, "unita": "oz" },
     *     { "nome": "lime juice", "quantita": 1, "unita": "oz" },
     *     { "nome": "triple sec", "quantita": 0.5, "unita": "oz" }
     *   ],
     *   "preparazione": "Pour into a shaker with ice and shake"
     * }
     */
    @Operation(summary = "Crea un nuovo cocktail con ingredienti", description = "Crea un nuovo cocktail con almeno 2 ingredienti. Gli ingredienti non esistenti vengono creati automaticamente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cocktail creato con successo"),
            @ApiResponse(responseCode = "400", description = "Dati non validi - minimo 2 ingredienti richiesti")
    })
    @PostMapping
    public ResponseEntity<CocktailDTO> createCocktail(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dati del cocktail con ingredienti", required = true)
            @RequestBody CreateCocktailRequestDTO requestDTO) {
        try {
            CocktailDTO created = cocktailService.createCocktail(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * PUT /api/cocktails/{id} - Updates a cocktail
     */
    @Operation(summary = "Aggiorna un cocktail", description = "Aggiorna i dati di un cocktail esistente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cocktail aggiornato con successo"),
            @ApiResponse(responseCode = "404", description = "Cocktail non trovato"),
            @ApiResponse(responseCode = "400", description = "Dati non validi")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CocktailDTO> updateCocktail(
            @Parameter(description = "ID del cocktail da aggiornare")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dati aggiornati del cocktail", required = true)
            @RequestBody CocktailDTO cocktailDTO) {
        try {
            Optional<CocktailDTO> updated = cocktailService.updateCocktail(id, cocktailDTO);
            return updated.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            // Log the error for debugging
            System.err.println("Errore durante l'aggiornamento del cocktail " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * DELETE /api/cocktails/{id} - Deletes a cocktail
     */
    @Operation(summary = "Elimina un cocktail", description = "Elimina un cocktail dal sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cocktail eliminato con successo"),
            @ApiResponse(responseCode = "404", description = "Cocktail non trovato")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCocktail(
            @Parameter(description = "ID del cocktail da eliminare")
            @PathVariable Long id) {
        if (cocktailService.deleteCocktail(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    




}
