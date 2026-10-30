package com.cocktail.cocktailproject.repository;

import com.cocktail.cocktailproject.entity.Ingrediente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * IngredienteRepository - Data access for the ingredients table
 * 
 * Automatic operations (from JpaRepository):
 * - findAll(): get all ingredients
 * - findById(id): get an ingredient by ID
 * - save(ingrediente): create or update an ingredient
 * - deleteById(id): delete an ingredient
 * 
 * Custom queries:
 * - Search by exact name, case-insensitive
 */
@Repository
public interface IngredienteRepository extends JpaRepository<Ingrediente, Long> {
    
    /**
     * Searches for an ingredient by name, ignoring case (recommended).
     * @param nome name to search for ("gin" finds "Gin", "GIN", "gin")
     * @return Optional containing the ingredient if found
     */
    Optional<Ingrediente> findByNomeIgnoreCase(String nome);
    
    /**
     * Searches ingredients by partial name with pagination (case-insensitive).
     * @param nome string to search for in the name ("lim" finds "lime", "limone", etc.)
     * @param pageable pagination parameters
     * @return page of ingredients matching the search
     */
    Page<Ingrediente> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
