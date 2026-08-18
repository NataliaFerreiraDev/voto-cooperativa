package br.com.nataliafdangelo.votocooperativa.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ItemSelecao(String texto, String url, Map<String, Object> body) {

    public ItemSelecao(String texto, String url) {
        this(texto, url, null);
    }

}
