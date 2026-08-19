package br.com.nataliafdangelo.votocooperativa.controller;

import br.com.nataliafdangelo.votocooperativa.client.CpfEligibilidadeClient;
import br.com.nataliafdangelo.votocooperativa.client.StatusVoto;
import br.com.nataliafdangelo.votocooperativa.dto.TelaSelecao;
import br.com.nataliafdangelo.votocooperativa.service.PautaService;
import br.com.nataliafdangelo.votocooperativa.service.VotoService;
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

@WebMvcTest(VotoController.class)
class VotoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VotoService votoService;

    @MockitoBean
    private PautaService pautaService;

    @MockitoBean
    private CpfEligibilidadeClient cpfEligibilidadeClient;

    @Test
    void deveRetornarFormularioDeInformarAssociado() throws Exception {
        mockMvc.perform(post("/api/v1/pautas/1/votos/novo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("FORMULARIO"))
                .andExpect(jsonPath("$.itens", hasSize(1)))
                .andExpect(jsonPath("$.itens[0].id").value("associadoId"))
                .andExpect(jsonPath("$.botaoOk.url").value("/api/v1/pautas/1/votos/opcoes"))
                .andExpect(jsonPath("$.botaoCancelar.url").value("/api/v1/pautas/1/menu"));
    }

    @Test
    void deveRetornarTelaDeOpcoesComAssociadoNaUrlQuandoApto() throws Exception {
        // given
        when(cpfEligibilidadeClient.verificar("12345678900")).thenReturn(StatusVoto.ABLE_TO_VOTE);

        // when / then
        mockMvc.perform(post("/api/v1/pautas/1/votos/opcoes")
                        .contentType("application/json")
                        .content("{\"associadoId\": \"12345678900\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("SELECAO"))
                .andExpect(jsonPath("$.itens", hasSize(2)))
                .andExpect(jsonPath("$.itens[0].url").value("/api/v1/pautas/1/votos/12345678900"))
                .andExpect(jsonPath("$.itens[0].body.opcao").value("SIM"))
                .andExpect(jsonPath("$.itens[1].body.opcao").value("NAO"));
    }

    @Test
    void deveRejeitarQuandoAssociadoNaoAptoAVotar() throws Exception {
        // given
        when(cpfEligibilidadeClient.verificar("12345678900")).thenReturn(StatusVoto.UNABLE_TO_VOTE);

        // when / then
        mockMvc.perform(post("/api/v1/pautas/1/votos/opcoes")
                        .contentType("application/json")
                        .content("{\"associadoId\": \"12345678900\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRejeitarAssociadoEmBranco() throws Exception {
        mockMvc.perform(post("/api/v1/pautas/1/votos/opcoes")
                        .contentType("application/json")
                        .content("{\"associadoId\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRejeitarAssociadoComFormatoInvalido() throws Exception {
        mockMvc.perform(post("/api/v1/pautas/1/votos/opcoes")
                        .contentType("application/json")
                        .content("{\"associadoId\": \"123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRegistrarVotoComSucesso() throws Exception {
        // given
        when(pautaService.menu(1L)).thenReturn(new TelaSelecao("Pauta", List.of()));

        // when / then
        mockMvc.perform(post("/api/v1/pautas/1/votos/12345678900")
                        .contentType("application/json")
                        .content("{\"opcao\": \"SIM\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("SELECAO"));
    }

    @Test
    void deveRejeitarVotoSemOpcao() throws Exception {
        mockMvc.perform(post("/api/v1/pautas/1/votos/12345678900")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

}
