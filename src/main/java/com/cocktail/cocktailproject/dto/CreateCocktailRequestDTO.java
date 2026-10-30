package com.cocktail.cocktailproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * CreateCocktailRequestDTO - DTO for creating a new cocktail
 * 
 * Contains:
 * - Cocktail data (name, description, time, notes)
 * - List of ingredients with quantities and units
 * - Preparation (optional - default: "Mescolare gli ingredienti")
 * 
 * Esempio JSON:
 * {
 *   "nome": "Margarita",
 *   "descrizione": "Classic Mexican cocktail",
 *   "tempoPreparazioneMinutes": 5,
 *   "note": "Serve chilled",
 *   "ingredienti": [
 *     { "nome": "tequila", "quantita": 2, "unita": "oz" },
 *     { "nome": "lime juice", "quantita": 1, "unita": "oz" },
 *     { "nome": "triple sec", "quantita": 0.5, "unita": "oz" }
 *   ],
 *   "preparazione": "Pour into a shaker with ice. Shake and pour into a glass with ice"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCocktailRequestDTO {
    
    private String nome;
    private String descrizione;
    private Integer tempoPreparazioneMinutes;
    private String note;
    private List<IngredienteStepDTO> ingredienti;
    private String preparazione;
    
    /**
     * IngredienteStepDTO - Represents an ingredient with its cocktail quantity
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngredienteStepDTO {
        private String nome;
        private BigDecimal quantita;
        private String unita;
    }
}
