package br.com.nataliafdangelo.votocooperativa.controller;

import br.com.nataliafdangelo.votocooperativa.dto.*;
import br.com.nataliafdangelo.votocooperativa.service.PautaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/novo")
    public TelaFormulario telaNovaPauta() {
        return new TelaFormulario(
                "Nova pauta",
                List.of(
                        ItemFormulario.inputTexto("titulo", "Título"),
                        ItemFormulario.inputTexto("descricao", "Descrição")
                ),
                new Botao("Cadastrar", "/api/v1/pautas", Map.of())
        );
    }

    @PostMapping
    public TelaSelecao criar(@Valid @RequestBody CriarPautaRequest request) {
        return pautaService.criar(request);
    }

}
