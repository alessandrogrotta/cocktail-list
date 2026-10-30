package com.cocktail.cocktailproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngredientiDTO {
    private Long id;
    private String nome;

    public IngredientiDTO(String nome, Long id) {
        this.nome = nome;
        this.id = id;
    }

}
