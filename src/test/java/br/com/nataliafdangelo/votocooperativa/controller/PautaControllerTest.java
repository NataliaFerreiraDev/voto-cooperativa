package br.com.nataliafdangelo.votocooperativa.controller;

import br.com.nataliafdangelo.votocooperativa.dto.CriarPautaRequest;
import br.com.nataliafdangelo.votocooperativa.dto.ItemSelecao;
import br.com.nataliafdangelo.votocooperativa.dto.TelaSelecao;
import br.com.nataliafdangelo.votocooperativa.service.PautaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PautaController.class)
class PautaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PautaService pautaService;

    @Test
    void deveListarPautas() throws Exception {
        // given
        TelaSelecao telaEsperada = new TelaSelecao("Pautas cadastradas",
                List.of(new ItemSelecao("Pauta teste — aguardando abertura de sessão", "/api/v1/pautas/1/menu")));
        when(pautaService.listar()).thenReturn(telaEsperada);

        // when / then
        mockMvc.perform(get("/api/v1/pautas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("SELECAO"))
                .andExpect(jsonPath("$.itens[0].texto").value("Pauta teste — aguardando abertura de sessão"));
    }

    @Test
    void deveRetornarFormularioDeNovaPauta() throws Exception {
        mockMvc.perform(post("/api/v1/pautas/novo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("FORMULARIO"))
                .andExpect(jsonPath("$.titulo").value("Nova pauta"))

                .andExpect(jsonPath("$.itens[0].id").value("titulo"))
                .andExpect(jsonPath("$.itens[1].id").value("descricao"))
                .andExpect(jsonPath("$.botaoOk.texto").value("Cadastrar"))
                .andExpect(jsonPath("$.botaoOk.url").value("/api/v1/pautas"));
    }

    @Test
    void deveCriarPautaComSucesso() throws Exception {
        // given
        TelaSelecao telaAtualizada = new TelaSelecao("Pautas cadastradas",
                List.of(new ItemSelecao("Reforma do estatuto — aguardando abertura de sessão", "/api/v1/pautas/1/menu")));
        when(pautaService.criar(any(CriarPautaRequest.class))).thenReturn(telaAtualizada);

        String corpoValido = """
                {"titulo": "Reforma do estatuto", "descricao": "desc"}
                """;

        // when / then
        mockMvc.perform(post("/api/v1/pautas")
                        .contentType("application/json")
                        .content(corpoValido))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("SELECAO"))
                .andExpect(jsonPath("$.itens[0].texto").value("Reforma do estatuto — aguardando abertura de sessão"));
    }

    @Test
    void deveRejeitarCadastroComTituloEmBranco() throws Exception {
        String corpoInvalido = """
                {"titulo": "", "descricao": "desc"}
                """;

        mockMvc.perform(post("/api/v1/pautas")
                        .contentType("application/json")
                        .content(corpoInvalido))
                .andExpect(status().isBadRequest());
    }

}
