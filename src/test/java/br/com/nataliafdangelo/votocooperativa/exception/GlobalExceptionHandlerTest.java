package br.com.nataliafdangelo.votocooperativa.exception;

import br.com.nataliafdangelo.votocooperativa.client.CpfEligibilidadeClient;
import br.com.nataliafdangelo.votocooperativa.controller.PautaController;
import br.com.nataliafdangelo.votocooperativa.controller.ResultadoController;
import br.com.nataliafdangelo.votocooperativa.controller.SessaoVotacaoController;
import br.com.nataliafdangelo.votocooperativa.controller.VotoController;
import br.com.nataliafdangelo.votocooperativa.domain.OpcaoVoto;
import br.com.nataliafdangelo.votocooperativa.service.PautaService;
import br.com.nataliafdangelo.votocooperativa.service.ResultadoService;
import br.com.nataliafdangelo.votocooperativa.service.SessaoVotacaoService;
import br.com.nataliafdangelo.votocooperativa.service.VotoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        PautaController.class,
        SessaoVotacaoController.class,
        VotoController.class,
        ResultadoController.class
})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PautaService pautaService;

    @MockitoBean
    private SessaoVotacaoService sessaoVotacaoService;

    @MockitoBean
    private VotoService votoService;

    @MockitoBean
    private ResultadoService resultadoService;

    @MockitoBean
    private CpfEligibilidadeClient cpfEligibilidadeClient;

    @Test
    void deveTraduzirPautaNaoEncontradaPara404() throws Exception {
        // given
        when(pautaService.menu(99L)).thenThrow(new PautaNaoEncontradaException(99L));

        // when / then
        mockMvc.perform(post("/api/v1/pautas/99/menu"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.titulo").value("Pauta não encontrada"))
                .andExpect(jsonPath("$.mensagem").value("Pauta não encontrada: 99"));
    }

    @Test
    void deveTraduzirSessaoJaAbertaPara409() throws Exception {
        // given
        doThrow(new SessaoJaAbertaException(1L)).when(sessaoVotacaoService).abrir(anyLong(), any());

        // when / then
        mockMvc.perform(post("/api/v1/pautas/1/sessoes")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.titulo").value("Sessão já aberta"));
    }

    @Test
    void deveTraduzirSessaoFechadaPara422() throws Exception {
        // given
        doThrow(new SessaoFechadaException(1L)).when(votoService).votar(1L, "12345678900", OpcaoVoto.SIM);

        // when / then
        mockMvc.perform(post("/api/v1/pautas/1/votos/12345678900")
                        .contentType("application/json")
                        .content("{\"opcao\": \"SIM\"}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.titulo").value("Sessão fechada"));
    }

    @Test
    void deveTraduzirVotoDuplicadoPara409() throws Exception {
        // given
        doThrow(new VotoDuplicadoException(1L, "12345678900"))
                .when(votoService).votar(1L, "12345678900", OpcaoVoto.SIM);

        // when / then
        mockMvc.perform(post("/api/v1/pautas/1/votos/12345678900")
                        .contentType("application/json")
                        .content("{\"opcao\": \"SIM\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.titulo").value("Voto duplicado"));
    }

    @Test
    void deveTraduzirSessaoAindaAbertaPara422() throws Exception {
        // given
        when(resultadoService.resultado(1L)).thenThrow(new SessaoAindaAbertaException(1L));

        // when / then
        mockMvc.perform(post("/api/v1/pautas/1/resultado"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.titulo").value("Sessão ainda aberta"));
    }

    @Test
    void deveTraduzirCpfInvalidoPara404() throws Exception {
        // given
        when(cpfEligibilidadeClient.verificar("12345678900")).thenThrow(new CpfInvalidoException("12345678900"));

        // when / then
        mockMvc.perform(post("/api/v1/pautas/1/votos/opcoes")
                        .contentType("application/json")
                        .content("{\"associadoId\": \"12345678900\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.titulo").value("CPF inválido"));
    }

    @Test
    void deveTraduzirServicoCpfIndisponivelPara503() throws Exception {
        // given
        when(cpfEligibilidadeClient.verificar("12345678900"))
                .thenThrow(new ServicoCpfIndisponivelException(new RuntimeException("timeout")));

        // when / then
        mockMvc.perform(post("/api/v1/pautas/1/votos/opcoes")
                        .contentType("application/json")
                        .content("{\"associadoId\": \"12345678900\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.titulo").value("Serviço indisponível"));
    }

    @Test
    void deveTraduzirCorpoInvalidoPara400() throws Exception {
        mockMvc.perform(post("/api/v1/pautas")
                        .contentType("application/json")
                        .content("{ isto não é json válido"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.titulo").value("Requisição inválida"));
    }

    @Test
    void deveTraduzirCampoInvalidoPara400ComMensagemDoCampo() throws Exception {
        mockMvc.perform(post("/api/v1/pautas")
                        .contentType("application/json")
                        .content("{\"titulo\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.titulo").value("Dados inválidos"))
                .andExpect(jsonPath("$.mensagem").exists());
    }

    @Test
    void deveTraduzirErroInesperadoPara500SemVazarStacktrace() throws Exception {
        // given
        when(pautaService.listar()).thenThrow(new IllegalStateException("detalhe interno sensível"));

        // when / then
        mockMvc.perform(post("/api/v1/pautas/lista"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.titulo").value("Erro interno"))
                .andExpect(jsonPath("$.mensagem").value("Ocorreu um erro inesperado. Tente novamente."))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("detalhe interno sensível"))));
    }

}
