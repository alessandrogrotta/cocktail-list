package com.cocktail.cocktailproject.service;

import com.cocktail.cocktailproject.dto.CocktailDTO;
import com.cocktail.cocktailproject.entity.Cocktail;
import com.cocktail.cocktailproject.entity.UserFavorito;
import com.cocktail.cocktailproject.repository.CocktailRepository;
import com.cocktail.cocktailproject.repository.UserFavoritoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * FavoritiService - Favorite cocktail management for authenticated users
 * 
 * How it works:
 * - The user ID (UUID) is extracted from the Keycloak JWT (claim 'sub')
 * - Each user can have a personal list of favorite cocktails
 * - The user-cocktail relationship is many-to-many (one user -> many favorite cocktails)
 * - Favorites are persisted in the user_favoriti table
 * 
 * Constraints:
 * - A user cannot add the same cocktail to favorites twice
 * - If a cocktail is deleted, its associated favorites are deleted in cascade
 */
@Service
public class FavoritiService {
    
    @Autowired
    private UserFavoritoRepository favoritiRepository;
    
    @Autowired
    private CocktailRepository cocktailRepository;
    
    @Autowired
    private CocktailService cocktailService;
    
    /**
     * Adds a cocktail to the user's favorites.
     * @param keycloakUserId user UUID from the JWT (claim 'sub')
     * @param cocktailId cocktail ID
     */
    @Transactional
    public void aggiungiPreferito(String keycloakUserId, Long cocktailId) {
        // Verify that the cocktail exists
        Cocktail cocktail = cocktailRepository.findById(cocktailId)
                .orElseThrow(() -> new RuntimeException("Cocktail non trovato con ID: " + cocktailId));
        
        // Check whether it is already a favorite
        if (favoritiRepository.existsByKeycloakUserIdAndCocktailId(keycloakUserId, cocktailId)) {
            throw new RuntimeException("Cocktail già presente nei favoriti");
        }
        
        // Create and save the favorite
        UserFavorito favorito = new UserFavorito();
        favorito.setKeycloakUserId(keycloakUserId);
        favorito.setCocktail(cocktail);
        favoritiRepository.save(favorito);
    }
    
    /**
     * Removes a cocktail from favorites.
     * @param keycloakUserId user UUID from the JWT (claim 'sub')
     * @param cocktailId cocktail ID
     */
    @Transactional
    public void rimuoviPreferito(String keycloakUserId, Long cocktailId) {
        UserFavorito favorito = favoritiRepository
                .findByKeycloakUserIdAndCocktailId(keycloakUserId, cocktailId)
                .orElseThrow(() -> new RuntimeException("Favorito non trovato"));
        
        favoritiRepository.delete(favorito);
    }
    
    /**
     * Gets all favorite cocktails for a user.
     * @param keycloakUserId user UUID from the JWT (claim 'sub')
     * @return list of favorite cocktails
     */
    public List<CocktailDTO> getPreferiti(String keycloakUserId) {
        List<UserFavorito> favoriti = favoritiRepository.findByKeycloakUserId(keycloakUserId);
        
        return favoriti.stream()
                .map(fav -> cocktailService.getCocktailById(fav.getCocktail().getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
    
    /**
     * Checks whether a cocktail is in a user's favorites.
     * @param keycloakUserId user UUID from the JWT (claim 'sub')
     * @param cocktailId cocktail ID
     * @return true if it is a favorite, false otherwise
     */
    public boolean isPreferito(String keycloakUserId, Long cocktailId) {
        return favoritiRepository.existsByKeycloakUserIdAndCocktailId(keycloakUserId, cocktailId);
    }
    
    /**
     * Counts a user's favorites.
     * @param keycloakUserId user UUID from the JWT (claim 'sub')
     * @return number of cocktails in the favorites
     */
    public long contaPreferiti(String keycloakUserId) {
        return favoritiRepository.countByKeycloakUserId(keycloakUserId);
    }
    
    /**
     * Toggle: adds the cocktail to favorites if absent, otherwise removes it.
     * Ideal for frontends with an on/off button.
     * 
     * @param keycloakUserId user UUID from the JWT (claim 'sub')
     * @param cocktailId cocktail ID
     * @return true if added, false if removed
     */
    @Transactional
    public boolean togglePreferito(String keycloakUserId, Long cocktailId) {
        // Verify that the cocktail exists
        Cocktail cocktail = cocktailRepository.findById(cocktailId)
                .orElseThrow(() -> new RuntimeException("Cocktail non trovato con ID: " + cocktailId));
        
        // Check whether it is already a favorite
        Optional<UserFavorito> esistente = favoritiRepository
                .findByKeycloakUserIdAndCocktailId(keycloakUserId, cocktailId);
        
        if (esistente.isPresent()) {
            // Remove from favorites
            favoritiRepository.delete(esistente.get());
            return false;  // false = rimosso
        } else {
            // Add to favorites
            UserFavorito favorito = new UserFavorito();
            favorito.setKeycloakUserId(keycloakUserId);
            favorito.setCocktail(cocktail);
            favoritiRepository.save(favorito);
            return true;  // true = aggiunto
        }
    }
}
