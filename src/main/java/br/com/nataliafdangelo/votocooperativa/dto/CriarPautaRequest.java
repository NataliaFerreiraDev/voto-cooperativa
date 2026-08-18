package br.com.nataliafdangelo.votocooperativa.dto;

import jakarta.validation.constraints.NotBlank;

public record CriarPautaRequest(@NotBlank String titulo, String descricao) {
}
