package com.cocktail.cocktailproject.repository;

import com.cocktail.cocktailproject.entity.Preparazione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * PreparazioneRepository - Data access for the preparation table
 * 
 * Automatic operations (from JpaRepository):
 * - findAll(): get all preparation steps
 * - save(preparazione): create or update a step
 * - deleteById(id): delete a step
 * 
 * Custom queries:
 * - Search steps by cocktail with ordering
 * - Delete all steps for a cocktail
 */
@Repository
public interface PreparazioneRepository extends JpaRepository<Preparazione, Long> {
    
    /**
     * Gets all preparation steps for a cocktail, ordered by step number.
     * @param cocktailId cocktail ID
     * @return list of ordered steps (step 1, step 2, step 3, ...)
     */
    List<Preparazione> findByCocktailIdOrderByStepOrderAsc(Long cocktailId);
    
    /**
     * Deletes all preparation steps for a cocktail.
     * Used before deleting a cocktail (cascade deletion).
     * 
     * @param cocktailId cocktail ID
     */
    void deleteByCocktailId(Long cocktailId);

    /**
     * Deletes all steps that use a specific ingredient.
     * Used before deleting an ingredient.
     *
     * @param ingredienteId ingredient ID
     */
    void deleteByIngredienteId(Long ingredienteId);
}
