package br.com.nataliafdangelo.votocooperativa.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TelaFormulario(String tipo, String titulo, List<ItemFormulario> itens,
                             Botao botaoOk, Botao botaoCancelar) {

    public TelaFormulario {
        tipo = "FORMULARIO";
    }

    public TelaFormulario(String titulo, List<ItemFormulario> itens, Botao botaoOk) {
        this(null, titulo, itens, botaoOk, null);
    }

    public TelaFormulario(String titulo, List<ItemFormulario> itens, Botao botaoOk, Botao botaoCancelar) {
        this(null, titulo, itens, botaoOk, botaoCancelar);
    }

}
