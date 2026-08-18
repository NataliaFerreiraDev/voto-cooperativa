package br.com.nataliafdangelo.votocooperativa.controller;

import br.com.nataliafdangelo.votocooperativa.dto.Botao;
import br.com.nataliafdangelo.votocooperativa.dto.CriarPautaRequest;
import br.com.nataliafdangelo.votocooperativa.dto.ItemFormulario;
import br.com.nataliafdangelo.votocooperativa.dto.TelaFormulario;
import br.com.nataliafdangelo.votocooperativa.dto.TelaSelecao;
import br.com.nataliafdangelo.votocooperativa.service.PautaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pautas")
public class PautaController {

    private final PautaService pautaService;

    public PautaController(PautaService pautaService) {
        this.pautaService = pautaService;
    }

    @GetMapping
    public TelaSelecao listar() {
        return pautaService.listar();
    }

    @PostMapping("/lista")
    public TelaSelecao listarViaPost() {
        return pautaService.listar();
    }

    @PostMapping("/novo")
    public TelaFormulario telaNovaPauta() {
        return new TelaFormulario(
                "Nova pauta",
                List.of(
                        ItemFormulario.inputTexto("titulo", "Título"),
                        ItemFormulario.inputTexto("descricao", "Descrição")
                ),
                new Botao("Cadastrar", "/api/v1/pautas", Map.of()),
                new Botao("Cancelar", "/api/v1/pautas/lista", Map.of())
        );
    }

    @PostMapping
    public TelaSelecao criar(@Valid @RequestBody CriarPautaRequest request) {
        return pautaService.criar(request);
    }

    @PostMapping("/{id}/menu")
    public TelaSelecao menu(@PathVariable Long id) {
        return pautaService.menu(id);
    }

}
