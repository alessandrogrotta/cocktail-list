package com.cocktail.cocktailproject.service;

import com.cocktail.cocktailproject.dto.IngredientiDTO;
import com.cocktail.cocktailproject.entity.Ingrediente;
import com.cocktail.cocktailproject.repository.IngredienteRepository;
import com.cocktail.cocktailproject.repository.PreparazioneRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * IngredientService - Business logic for managing ingredients.
 *
 * Responsibilities:
 * - Provide paginated ingredient read operations
 * - Safely delete ingredients by first removing linked preparation steps
 * - Perform essential Entity -> DTO conversions
 */
@Service
public class IngredientService {

    private final IngredienteRepository ingredienteRepository;
    private final PreparazioneRepository preparazioneRepository;

    /**
     * Constructor using repository dependency injection.
     */
    public IngredientService(IngredienteRepository ingredienteRepository,
                             PreparazioneRepository preparazioneRepository) {
        this.ingredienteRepository = ingredienteRepository;
        this.preparazioneRepository = preparazioneRepository;
    }

    /**
     * Returns a page of ingredients.
     * @param pageable pagination parameters
     * @return page of IngredientiDTO objects
     */
    public Page<IngredientiDTO> getAllIngredients(Pageable pageable) {
        return ingredienteRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    /**
     * Searches ingredients by name with pagination (case-insensitive partial search).
     * @param nome string to search for in the ingredient name
     * @param pageable pagination parameters
     * @return page of IngredientiDTO objects matching the search
     */
    public Page<IngredientiDTO> searchByName(String nome, Pageable pageable) {
        return ingredienteRepository.findByNomeContainingIgnoreCase(nome, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Deletes an ingredient by ID, first removing references from preparation steps.
     * @param id ID of the ingredient to delete
     * @return true if deleted, false if it does not exist
     */
    @Transactional
    public boolean deleteIngredient(Long id) {
        if (!ingredienteRepository.existsById(id)) {
            return false;
        }
        // Delete preparation steps referencing the ingredient, then delete the ingredient
        preparazioneRepository.deleteByIngredienteId(id);
        ingredienteRepository.deleteById(id);
        return true;
    }

    /**
     * Minimal conversion from Ingrediente entity to DTO.
     */
    private IngredientiDTO convertToDTO(Ingrediente ingrediente) {
        return new IngredientiDTO(ingrediente.getId(), ingrediente.getNome());
    }
}
