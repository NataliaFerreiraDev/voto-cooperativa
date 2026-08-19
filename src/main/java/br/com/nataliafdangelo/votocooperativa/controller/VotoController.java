package br.com.nataliafdangelo.votocooperativa.controller;

import br.com.nataliafdangelo.votocooperativa.dto.Botao;
import br.com.nataliafdangelo.votocooperativa.dto.IdentificarAssociadoRequest;
import br.com.nataliafdangelo.votocooperativa.dto.ItemFormulario;
import br.com.nataliafdangelo.votocooperativa.dto.ItemSelecao;
import br.com.nataliafdangelo.votocooperativa.dto.TelaFormulario;
import br.com.nataliafdangelo.votocooperativa.dto.TelaSelecao;
import br.com.nataliafdangelo.votocooperativa.dto.VotarRequest;
import br.com.nataliafdangelo.votocooperativa.service.PautaService;
import br.com.nataliafdangelo.votocooperativa.service.VotoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pautas/{pautaId}/votos")
public class VotoController {

    private static final String BASE_URL_PAUTAS = "/api/v1/pautas/";

    private final VotoService votoService;
    private final PautaService pautaService;

    public VotoController(VotoService votoService, PautaService pautaService) {
        this.votoService = votoService;
        this.pautaService = pautaService;
    }

    @PostMapping("/novo")
    public TelaFormulario telaInformarAssociado(@PathVariable Long pautaId) {
        return new TelaFormulario(
                "Votar",
                List.of(ItemFormulario.inputTexto("associadoId", "Informe seu ID de associado")),
                new Botao("Continuar", BASE_URL_PAUTAS + pautaId + "/votos/opcoes", Map.of()),
                new Botao("Cancelar", BASE_URL_PAUTAS + pautaId + "/menu", Map.of())
        );
    }

    @PostMapping("/opcoes")
    public TelaSelecao telaOpcoesDeVoto(@PathVariable Long pautaId,
                                         @Valid @RequestBody IdentificarAssociadoRequest request) {
        String associadoId = request.associadoId();
        votoService.verificarElegibilidade(associadoId);

        String base = BASE_URL_PAUTAS + pautaId + "/votos/" + associadoId;
        return new TelaSelecao("Como deseja votar?", List.of(
                new ItemSelecao("Sim", base, Map.of("opcao", "SIM")),
                new ItemSelecao("Não", base, Map.of("opcao", "NAO"))
        ));
    }

    @PostMapping("/{associadoId}")
    public TelaSelecao votar(@PathVariable Long pautaId,
                              @PathVariable String associadoId,
                              @Valid @RequestBody VotarRequest request) {
        votoService.votar(pautaId, associadoId, request.opcao());
        return pautaService.menu(pautaId);
    }

}
