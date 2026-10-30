package com.cocktail.cocktailproject.repository;

import com.cocktail.cocktailproject.entity.UserFavorito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * UserFavoritoRepository - Data access for the user_favoriti table
 * 
 * Manages the many-to-many relationship between users (Keycloak) and cocktails.
 * The user ID is the UUID extracted from the JWT (claim 'sub').
 * 
 * Automatic operations (from JpaRepository):
 * - findAll(): get all favorites for all users
 * - save(favorito): add a new favorite
 * - delete(favorito): remove a favorite
 * 
 * Custom queries:
 * - Search favorites by user
 * - Check whether a favorite exists
 * - Count favorites by user
 * - Delete by cocktail
 */
@Repository
public interface UserFavoritoRepository extends JpaRepository<UserFavorito, Long> {
    
    /**
     * Finds all favorite cocktails for a user.
     * @param keycloakUserId user UUID from the JWT (claim 'sub')
     * @return list of all user favorites
     */
    List<UserFavorito> findByKeycloakUserId(String keycloakUserId);
    
    /**
     * Checks whether a cocktail is already in a user's favorites.
     * Useful for preventing duplicates and showing the "favorite" state in the frontend.
     * 
     * @param keycloakUserId user UUID from the JWT
     * @param cocktailId ID of the cocktail to check
     * @return true if it is already a favorite, false otherwise
     */
    boolean existsByKeycloakUserIdAndCocktailId(String keycloakUserId, Long cocktailId);
    
    /**
     * Finds a specific favorite (user + cocktail combination).
     * Used to remove an existing favorite.
     * 
     * @param keycloakUserId user UUID from the JWT
     * @param cocktailId cocktail ID
     * @return Optional containing the favorite if it exists
     */
    Optional<UserFavorito> findByKeycloakUserIdAndCocktailId(String keycloakUserId, Long cocktailId);
    
    /**
     * Counts how many cocktails a user has in favorites.
     * Useful for badges/counters in the frontend.
     * 
     * @param keycloakUserId user UUID from the JWT
     * @return total number of user favorites
     */
    long countByKeycloakUserId(String keycloakUserId);
    
    /**
     * Deletes all favorites associated with a cocktail.
     * 
     * Called automatically before deleting a cocktail (cascade deletion).
     * Prevents a foreign key constraint violation.
     * 
     * @param cocktailId ID of the cocktail from which to remove all favorites
     */
    void deleteByCocktailId(Long cocktailId);
}
