package br.com.nataliafdangelo.votocooperativa.controller;

import br.com.nataliafdangelo.votocooperativa.dto.Botao;
import br.com.nataliafdangelo.votocooperativa.dto.ItemFormulario;
import br.com.nataliafdangelo.votocooperativa.dto.TelaFormulario;
import br.com.nataliafdangelo.votocooperativa.service.ResultadoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ResultadoController.class)
class ResultadoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ResultadoService resultadoService;

    @Test
    void deveRetornarTelaDeResultado() throws Exception {
        // given
        TelaFormulario tela = new TelaFormulario("Pauta teste", List.of(
                ItemFormulario.texto("Sim: 3 voto(s)"),
                ItemFormulario.texto("Não: 1 voto(s)"),
                ItemFormulario.texto("Resultado: Aprovada")
        ), new Botao("Voltar", "/api/v1/pautas/lista", Map.of()));
        when(resultadoService.resultado(1L)).thenReturn(tela);

        // when / then
        mockMvc.perform(post("/api/v1/pautas/1/resultado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("FORMULARIO"))
                .andExpect(jsonPath("$.itens[2].texto").value("Resultado: Aprovada"));
    }

}
