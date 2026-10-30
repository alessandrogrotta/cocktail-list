package com.cocktail.cocktailproject.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ingrediente entity - Catalog of ingredients available in the system
 * 
 * Table: ingredienti
 * 
 * Fields:
 * - id: Auto-incrementing primary key
 * - nome: Ingredient name (max 150 characters, required, UNIQUE)
 * 
 * Constraints:
 * - The name must be unique (two ingredients cannot have the same name)
 * - Normalization: names are stored in lowercase to prevent duplicates
 * 
 * Relationships:
 * - 1:N with Preparazione (an ingredient can be used in many cocktails)
 * 
 * Note: Ingredients are NOT deleted when a cocktail is deleted,
 * because they can be reused in other cocktails.
 */
@Entity
@Table(name = "ingredienti")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ingrediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, unique = true, length = 150)
    private String nome;
}
