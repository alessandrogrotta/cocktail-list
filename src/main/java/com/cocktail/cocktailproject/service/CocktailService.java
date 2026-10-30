package com.cocktail.cocktailproject.service;

import com.cocktail.cocktailproject.dto.CocktailDTO;
import com.cocktail.cocktailproject.dto.CreateCocktailRequestDTO;
import com.cocktail.cocktailproject.entity.Cocktail;
import com.cocktail.cocktailproject.entity.Ingrediente;
import com.cocktail.cocktailproject.entity.Preparazione;
import com.cocktail.cocktailproject.repository.CocktailRepository;
import com.cocktail.cocktailproject.repository.IngredienteRepository;
import com.cocktail.cocktailproject.repository.PreparazioneRepository;
import com.cocktail.cocktailproject.repository.UserFavoritoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * CocktailService - Business logic for managing cocktails
 * 
 * Responsibilities:
 * - CRUD operations (Create, Read, Update, Delete) for cocktails
 * - Conversion between Entity (database) and DTO (client)
 * - Validation of incoming data
 * - Ingredient and preparation step management
 * - Cascade deletion (preparation steps + favorites)
 */
@Service
public class CocktailService {

    private final CocktailRepository cocktailRepository;
    private final IngredienteRepository ingredienteRepository;
    private final PreparazioneRepository preparazioneRepository;
    private final UserFavoritoRepository userFavoritoRepository;

    // Constructor injection (Spring 4.3+): more testable, immutable, and explicit
    public CocktailService(
            CocktailRepository cocktailRepository,
            IngredienteRepository ingredienteRepository,
            PreparazioneRepository preparazioneRepository,
            UserFavoritoRepository userFavoritoRepository) {
        this.cocktailRepository = cocktailRepository;
        this.ingredienteRepository = ingredienteRepository;
        this.preparazioneRepository = preparazioneRepository;
        this.userFavoritoRepository = userFavoritoRepository;
    }

    /**
     * Gets all cocktails with pagination (recommended method).
     * @param pageable pagination parameters (page, size, sort)
     * @return page of cocktails with metadata (totalElements, totalPages, etc.)
     */
    public Page<CocktailDTO> getAllCocktails(Pageable pageable) {
        return cocktailRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    /**
     * Gets a specific cocktail by ID.
     * @param id unique cocktail ID
     * @return Optional containing the cocktail if found, otherwise empty
     */
    public Optional<CocktailDTO> getCocktailById(Long id) {
        return cocktailRepository.findById(id)
                .map(this::convertToDTO);
    }

    /**
     * Searches cocktails by name with pagination (recommended method).
     * @param nome string to search for in the name (case-insensitive, partial search)
     * @param pageable pagination parameters
     * @return page of cocktails matching the search
     */
    public Page<CocktailDTO> searchByName(String nome, Pageable pageable) {
        return cocktailRepository.findByNomeContainingIgnoreCase(nome, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Creates a complete new cocktail with ingredients and preparation.
     * 
     * Validation:
     * - Name is required and must not be blank
     * - At least 2 ingredients are required
     * - No duplicate ingredients in the same request
     * 
     * Behavior:
     * - Missing ingredients are created automatically
     * - Ingredient names are normalized (lowercase, trim)
     * - Preparation steps are numbered automatically
     * 
     * @param requestDTO cocktail data with ingredient list and instructions
     * @return CocktailDTO for the newly created cocktail with all details
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public CocktailDTO createCocktail(CreateCocktailRequestDTO requestDTO) {
        // Validate that there are at least 2 ingredients
        if (requestDTO.getIngredienti() == null || requestDTO.getIngredienti().size() < 2) {
            throw new IllegalArgumentException("Il cocktail deve contenere almeno 2 ingredienti");
        }
        
        // Validate that the name is not blank
        if (requestDTO.getNome() == null || requestDTO.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome del cocktail non può essere vuoto");
        }
        
        // Create the Cocktail entity
        Cocktail cocktail = new Cocktail();
        cocktail.setNome(requestDTO.getNome().trim());
        cocktail.setDescrizione(requestDTO.getDescrizione());
        cocktail.setTempoPreparazioneMinutes(requestDTO.getTempoPreparazioneMinutes());
        cocktail.setNote(requestDTO.getNote());
        
        // Save the cocktail
        Cocktail savedCocktail = cocktailRepository.save(cocktail);
        
        // Process the ingredients
        Set<Long> processedIngredients = new HashSet<>();
        int stepOrder = 1;
        
        for (CreateCocktailRequestDTO.IngredienteStepDTO ingredienteStep : requestDTO.getIngredienti()) {
            // Normalize the ingredient name (lowercase, trim)
            String nomeNormalizzato = ingredienteStep.getNome().trim().toLowerCase();
            
            // Verify that there are no duplicates in the same request
            if (!processedIngredients.isEmpty()) {
                for (Long ingredId : processedIngredients) {
                    Ingrediente existing = ingredienteRepository.findById(ingredId).orElse(null);
                    if (existing != null && existing.getNome().toLowerCase().equals(nomeNormalizzato)) {
                        throw new IllegalArgumentException("Ingrediente duplicato: " + ingredienteStep.getNome());
                    }
                }
            }
            
            // Find the ingredient in the database (case-insensitive)
            Optional<Ingrediente> existingIngrediente = ingredienteRepository.findByNomeIgnoreCase(nomeNormalizzato);
            
            Ingrediente ingrediente;
            if (existingIngrediente.isPresent()) {
                ingrediente = existingIngrediente.get();
            } else {
                // Create a new ingredient
                ingrediente = new Ingrediente();
                ingrediente.setNome(nomeNormalizzato);
                ingrediente = ingredienteRepository.save(ingrediente);
            }
            
            processedIngredients.add(ingrediente.getId());
            
            // Create the preparation step
            Preparazione preparazione = new Preparazione();
            preparazione.setCocktailId(savedCocktail.getId());
            preparazione.setIngredienteId(ingrediente.getId());
            preparazione.setQuantita(ingredienteStep.getQuantita());
            preparazione.setUnita(ingredienteStep.getUnita());
            preparazione.setStepOrder(stepOrder);
            
            // Use the provided preparation, or the default if none was provided
            if (requestDTO.getPreparazione() != null && !requestDTO.getPreparazione().trim().isEmpty()) {
                preparazione.setIstruzione(requestDTO.getPreparazione().trim());
            } else {
                preparazione.setIstruzione("Mescolare gli ingredienti");
            }
            
            preparazioneRepository.save(preparazione);
            stepOrder++;
        }
        
        return convertToDTO(savedCocktail);
    }

    /**
     * Creates a new cocktail (legacy version without ingredients).
     * Use createCocktail(CreateCocktailRequestDTO) for the complete version.
     * 
     * @param cocktailDTO base cocktail data
     * @return CocktailDTO for the created cocktail
     */
    @Transactional
    public CocktailDTO createCocktail(CocktailDTO cocktailDTO) {
        Cocktail cocktail = convertToEntity(cocktailDTO);
        Cocktail saved = cocktailRepository.save(cocktail);
        return convertToDTO(saved);
    }

    /**
     * Updates an existing cocktail (supports partial updates).
     * 
     * Only non-null fields in the DTO are updated.
     * Other fields retain their previous values.
     * 
     * @param id ID of the cocktail to update
     * @param cocktailDTO data to update (null fields are ignored)
     * @return Optional containing the updated cocktail, or empty if not found
     */
    @Transactional
    public Optional<CocktailDTO> updateCocktail(Long id, CocktailDTO cocktailDTO) {
        return cocktailRepository.findById(id)
                .map(existing -> {
                    // Update only non-null fields received in the DTO
                    if (cocktailDTO.getNome() != null) {
                        existing.setNome(cocktailDTO.getNome());
                    }
                    if (cocktailDTO.getDescrizione() != null) {
                        existing.setDescrizione(cocktailDTO.getDescrizione());
                    }
                    if (cocktailDTO.getTempoPreparazioneMinutes() != null) {
                        existing.setTempoPreparazioneMinutes(cocktailDTO.getTempoPreparazioneMinutes());
                    }
                    if (cocktailDTO.getNote() != null) {
                        existing.setNote(cocktailDTO.getNote());
                    }
                    Cocktail updated = cocktailRepository.save(existing);
                    return convertToDTO(updated);
                });
    }

    /**
     * Deletes a cocktail and all related data (cascade deletion).
     * 
     * Deletion sequence:
     * 1. Delete all favorites pointing to the cocktail
     * 2. Delete all preparation steps
     * 3. Delete the cocktail itself
     * 
     * Note: ingredients are NOT deleted (they may be used by other cocktails)
     * 
     * @param id ID of the cocktail to delete
     * @return true if deleted successfully, false if not found
     */
    @Transactional
    public boolean deleteCocktail(Long id) {
        if (cocktailRepository.existsById(id)) {
            // First delete associated favorites
            userFavoritoRepository.deleteByCocktailId(id);
            // Then delete the preparation steps
            preparazioneRepository.deleteByCocktailId(id);
            // Finally delete the cocktail
            cocktailRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Converts a Cocktail entity to a DTO for the client.
     * 
     * Enriches the data with:
     * - Ordered preparation steps
     * - Ingredient name for each step (join with the ingredients table)
     * 
     * @param cocktail entity from the database
     * @return complete DTO ready for the client
     */
    private CocktailDTO convertToDTO(Cocktail cocktail) {
        CocktailDTO dto = new CocktailDTO();
        dto.setId(cocktail.getId());
        dto.setNome(cocktail.getNome());
        dto.setDescrizione(cocktail.getDescrizione());
        dto.setTempoPreparazioneMinutes(cocktail.getTempoPreparazioneMinutes());
        dto.setNote(cocktail.getNote());
        
        // Load ordered steps
        List<Preparazione> steps = preparazioneRepository.findByCocktailIdOrderByStepOrderAsc(cocktail.getId());
        
        // Load all ingredients once (avoids the N+1 query problem)
        Map<Long, String> ingredientiMap = new HashMap<>();
        for (Preparazione step : steps) {
            if (!ingredientiMap.containsKey(step.getIngredienteId())) {
                String nomeIngrediente = ingredienteRepository.findById(step.getIngredienteId())
                        .map(Ingrediente::getNome)
                        .orElse("Ingrediente sconosciuto");
                ingredientiMap.put(step.getIngredienteId(), nomeIngrediente);
            }
        }
        
        // Convert each step to StepPreparazioneDTO
        List<CocktailDTO.StepPreparazioneDTO> stepsDTO = steps.stream()
                .map(step -> new CocktailDTO.StepPreparazioneDTO(
                        step.getStepOrder(),
                        ingredientiMap.get(step.getIngredienteId()),
                        step.getQuantita() != null ? step.getQuantita().toString() : null,
                        step.getUnita(),
                        step.getIstruzione()
                ))
                .collect(Collectors.toList());
        
        dto.setPreparazione(stepsDTO);
        return dto;
    }

    /**
     * Converts a DTO to a Cocktail entity (base fields only).
     * @param dto DTO received from the client
     * @return Cocktail entity ready to be saved
     */
    private Cocktail convertToEntity(CocktailDTO dto) {
        Cocktail cocktail = new Cocktail();
        cocktail.setNome(dto.getNome());
        cocktail.setDescrizione(dto.getDescrizione());
        cocktail.setTempoPreparazioneMinutes(dto.getTempoPreparazioneMinutes());
        cocktail.setNote(dto.getNote());
        return cocktail;
    }

    
}