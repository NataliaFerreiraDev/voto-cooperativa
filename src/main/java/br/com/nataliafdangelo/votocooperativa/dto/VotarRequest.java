package br.com.nataliafdangelo.votocooperativa.dto;

import br.com.nataliafdangelo.votocooperativa.domain.OpcaoVoto;
import jakarta.validation.constraints.NotNull;

public record VotarRequest(@NotNull OpcaoVoto opcao) {
}
