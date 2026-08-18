package br.com.nataliafdangelo.votocooperativa.controller;

import br.com.nataliafdangelo.votocooperativa.dto.TelaSelecao;
import br.com.nataliafdangelo.votocooperativa.service.PautaService;
import br.com.nataliafdangelo.votocooperativa.service.SessaoVotacaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SessaoVotacaoController.class)
class SessaoVotacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SessaoVotacaoService sessaoVotacaoService;

    @MockitoBean
    private PautaService pautaService;

    @Test
    void deveRetornarFormularioDeAbrirSessao() throws Exception {
        mockMvc.perform(post("/api/v1/pautas/1/sessoes/novo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("FORMULARIO"))
                .andExpect(jsonPath("$.itens", hasSize(1)))
                .andExpect(jsonPath("$.botaoOk.url").value("/api/v1/pautas/1/sessoes"))
                .andExpect(jsonPath("$.botaoCancelar.url").value("/api/v1/pautas/1/menu"));
    }

    @Test
    void deveAbrirSessaoComSucesso() throws Exception {
        // given
        when(pautaService.menu(1L)).thenReturn(new TelaSelecao("Pauta", List.of()));

        // when / then
        mockMvc.perform(post("/api/v1/pautas/1/sessoes")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("SELECAO"));
    }

    @Test
    void deveRejeitarDuracaoInvalida() throws Exception {
        mockMvc.perform(post("/api/v1/pautas/1/sessoes")
                        .contentType("application/json")
                        .content("{\"duracaoMinutos\": 0}"))
                .andExpect(status().isBadRequest());
    }

}