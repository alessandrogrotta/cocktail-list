package com.cocktail.cocktailproject.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Preparazione entity - Represents a recipe STEP for a cocktail
 * 
 * Table: preparazione (junction table between cocktails and ingredients with additional data)
 * 
 * Fields:
 * - id: Auto-incrementing primary key
 * - cocktailId: FK to cocktail (required)
 * - ingredienteId: FK to ingredients (required)
 * - quantita: Numeric quantity (DECIMAL 8,2)
 * - unita: Unit of measure (e.g. "ml", "oz", "teaspoons", max 30 characters)
 * - stepOrder: Step number in the sequence (required, determines order)
 * - istruzione: Text instructions for the step (TEXT, optional)
 * 
 * Example for a Margarita:
 * Step 1: ingredienteId=1 (tequila), quantita=2.00, unita="oz", istruzione="Pour the tequila"
 * Step 2: ingredienteId=5 (lime juice), quantita=1.00, unita="oz", istruzione="Add lime"
 * Step 3: ingredienteId=7 (triple sec), quantita=0.50, unita="oz", istruzione="Add triple sec"
 * 
 * Relationships:
 * - N:1 with Cocktail (many steps belong to one cocktail)
 * - N:1 with Ingrediente (many steps use the same ingredient)
 * 
 * Cascade deletion: when a cocktail is deleted, all of its steps are deleted.
 */
@Entity
@Table(name = "preparazione")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Preparazione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cocktail_id", nullable = false)
    private Long cocktailId;

    @Column(name = "ingrediente_id", nullable = false)
    private Long ingredienteId;

    @Column(name = "quantita", precision = 8, scale = 2)
    private BigDecimal quantita;

    @Column(name = "unita", length = 30)
    private String unita;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(name = "istruzione", columnDefinition = "TEXT")
    private String istruzione;
}
