package br.com.nataliafdangelo.votocooperativa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record IdentificarAssociadoRequest(
        @NotBlank
        @Pattern(regexp = "\\d{11}", message = "deve conter 11 dígitos numéricos")
        String associadoId) {
}
