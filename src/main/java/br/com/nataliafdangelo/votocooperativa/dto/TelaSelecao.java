package br.com.nataliafdangelo.votocooperativa.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TelaSelecao(String tipo, String titulo, List<ItemSelecao> itens) {

    public TelaSelecao {
        tipo = "SELECAO";
    }

    public TelaSelecao(String titulo, List<ItemSelecao> itens) {
        this(null, titulo, itens);
    }

}
