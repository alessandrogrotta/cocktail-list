package com.cocktail.cocktailproject.repository;

import com.cocktail.cocktailproject.entity.Cocktail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CocktailRepository - Data access for the cocktail table
 * 
 * Automatic operations (from JpaRepository):
 * - findAll(): get all cocktails
 * - findById(id): get a cocktail by ID
 * - save(cocktail): create or update a cocktail
 * - deleteById(id): delete a cocktail
 * - existsById(id): check existence
 * 
 * Custom queries:
 * - Search by name with pagination support
 */
@Repository
public interface CocktailRepository extends JpaRepository<Cocktail, Long> {
    
    /**
     * Searches cocktails by name (case-insensitive, partial search) - without pagination.
     * @param nome string to search for (e.g. "marg" finds "Margarita")
     * @return list of all cocktails containing the string in their name
     */
    List<Cocktail> findByNomeContainingIgnoreCase(String nome);
    
    /**
     * Searches cocktails by name with pagination (recommended method).
     * @param nome string to search for (case-insensitive, partial search)
     * @param pageable pagination parameters (page, size, sort)
     * @return page of results
     */
    Page<Cocktail> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
