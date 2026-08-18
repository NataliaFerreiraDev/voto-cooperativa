package br.com.nataliafdangelo.votocooperativa.controller;

import br.com.nataliafdangelo.votocooperativa.dto.AbrirSessaoRequest;
import br.com.nataliafdangelo.votocooperativa.dto.Botao;
import br.com.nataliafdangelo.votocooperativa.dto.ItemFormulario;
import br.com.nataliafdangelo.votocooperativa.dto.TelaFormulario;
import br.com.nataliafdangelo.votocooperativa.dto.TelaSelecao;
import br.com.nataliafdangelo.votocooperativa.service.PautaService;
import br.com.nataliafdangelo.votocooperativa.service.SessaoVotacaoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pautas/{pautaId}/sessoes")
public class SessaoVotacaoController {

    private final SessaoVotacaoService sessaoVotacaoService;
    private final PautaService pautaService;

    public SessaoVotacaoController(SessaoVotacaoService sessaoVotacaoService, PautaService pautaService) {
        this.sessaoVotacaoService = sessaoVotacaoService;
        this.pautaService = pautaService;
    }

    @PostMapping("/novo")
    public TelaFormulario telaAbrirSessao(@PathVariable Long pautaId) {
        return new TelaFormulario(
                "Abrir sessão de votação",
                List.of(ItemFormulario.inputNumero("duracaoMinutos", "Duração em minutos (padrão: 1)")),
                new Botao("Abrir", "/api/v1/pautas/" + pautaId + "/sessoes", Map.of()),
                new Botao("Cancelar", "/api/v1/pautas/" + pautaId + "/menu", Map.of())
        );
    }

    @PostMapping
    public TelaSelecao abrir(@PathVariable Long pautaId, @Valid @RequestBody AbrirSessaoRequest request) {
        sessaoVotacaoService.abrir(pautaId, request);
        return pautaService.menu(pautaId);
    }

}
