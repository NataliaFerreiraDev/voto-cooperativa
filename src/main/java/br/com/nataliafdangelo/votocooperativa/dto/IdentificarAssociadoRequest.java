package br.com.nataliafdangelo.votocooperativa.dto;

import jakarta.validation.constraints.NotBlank;

public record IdentificarAssociadoRequest(@NotBlank String associadoId) {
}
