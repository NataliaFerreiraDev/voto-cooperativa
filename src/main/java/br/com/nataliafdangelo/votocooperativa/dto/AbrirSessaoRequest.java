package br.com.nataliafdangelo.votocooperativa.dto;

import jakarta.validation.constraints.Min;

public record AbrirSessaoRequest(@Min(1) Integer duracaoMinutos) {
}
