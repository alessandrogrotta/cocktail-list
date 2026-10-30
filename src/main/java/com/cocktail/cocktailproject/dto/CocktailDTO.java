package com.cocktail.cocktailproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * CocktailDTO - Data Transfer Object for cocktails
 * 
 * Used to transfer data between the controller and HTTP clients.
 * Combines cocktail data with a formatted list of StepPreparazioneDTO objects.
 * DTOs hide database details and present clean, formatted data to the client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CocktailDTO {
    private Long id;
    private String nome;
    private String descrizione;
    private Integer tempoPreparazioneMinutes;
    private String note;
    private List<StepPreparazioneDTO> preparazione;

    /**
     * StepPreparazioneDTO - One recipe preparation step
     * Example: 25 ml gin, 2 oz tequila, etc.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepPreparazioneDTO {
        private Integer stepOrder;
        private String ingrediente;
        private String quantita;
        private String unita;
        private String istruzione;
    }
}
