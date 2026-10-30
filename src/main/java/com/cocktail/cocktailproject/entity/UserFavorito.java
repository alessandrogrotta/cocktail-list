package com.cocktail.cocktailproject.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * UserFavorito entity - Represents a cocktail in a user's favorites
 * 
 * Table: user_favoriti (junction table for the User-Cocktail many-to-many relationship)
 * 
 * Fields:
 * - id: Auto-incrementing primary key
 * - keycloakUserId: User UUID extracted from the Keycloak JWT ('sub' claim)
 * - cocktail: ManyToOne relationship to the favorited cocktail
 * - createdAt: Timestamp of when it was added to favorites
 * 
 * How it works:
 * - When a user authenticates, the JWT contains a 'sub' claim with the unique UUID
 * - The UUID is stored in the keycloak_user_id column
 * - The app does not need a "users" table: users exist only in Keycloak
 * 
 * Constraints:
 * - A user cannot add the same cocktail twice (handled at service level)
 * - If a cocktail is deleted, its associated favorites are deleted in cascade
 * 
 * Relationships:
 * - N:1 with Cocktail (many favorites point to the same cocktail)
 */
@Entity
@Table(name = "user_favoriti")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFavorito {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "keycloak_user_id", nullable = false, length = 255)
    private String keycloakUserId;  // User UUID from the JWT (claim 'sub')
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cocktail_id", nullable = false)
    private Cocktail cocktail;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
