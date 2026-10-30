package com.cocktail.cocktailproject.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cocktail entity - Represents a cocktail in the database
 * 
 * Table: cocktail
 * 
 * Fields:
 * - id: Auto-incrementing primary key
 * - nome: Cocktail name (max 150 characters, required)
 * - descrizione: Detailed description (TEXT, optional)
 * - tempoPreparazioneMinutes: Preparation time in minutes (optional)
 * - note: Additional notes (TEXT, optional)
 * 
 * Relationships:
 * - 1:N with Preparazione (a cocktail has many preparation steps)
 * - M:N with Ingrediente (through the Preparazione table)
 * - 1:N with UserFavorito (a cocktail can be favorited by many users)
 */
@Entity
@Table(name = "cocktail")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cocktail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "descrizione", columnDefinition = "TEXT")
    private String descrizione;

    @Column(name = "tempo_preparazione_minutes")
    private Integer tempoPreparazioneMinutes;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
}
