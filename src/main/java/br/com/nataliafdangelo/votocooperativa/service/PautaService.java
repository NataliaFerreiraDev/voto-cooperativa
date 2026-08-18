package br.com.nataliafdangelo.votocooperativa.service;

import br.com.nataliafdangelo.votocooperativa.domain.Pauta;
import br.com.nataliafdangelo.votocooperativa.dto.CriarPautaRequest;
import br.com.nataliafdangelo.votocooperativa.dto.ItemSelecao;
import br.com.nataliafdangelo.votocooperativa.dto.TelaSelecao;
import br.com.nataliafdangelo.votocooperativa.repository.PautaRepository;
import br.com.nataliafdangelo.votocooperativa.repository.SessaoVotacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class PautaService {

    private static final Logger log = LoggerFactory.getLogger(PautaService.class);

    private final PautaRepository pautaRepository;
    private final SessaoVotacaoRepository sessaoVotacaoRepository;
    private final Clock clock;

    public PautaService(PautaRepository pautaRepository,
                        SessaoVotacaoRepository sessaoVotacaoRepository,
                        Clock clock) {
        this.pautaRepository = pautaRepository;
        this.sessaoVotacaoRepository = sessaoVotacaoRepository;
        this.clock = clock;
    }

    public TelaSelecao listar() {
        List<ItemSelecao> itens = pautaRepository.findAll().stream()
                .map(this::paraItem)
                .toList();
        return new TelaSelecao("Pautas cadastradas", itens);
    }

    public TelaSelecao criar(CriarPautaRequest request) {
        Pauta pauta = new Pauta(request.titulo(), request.descricao(), Instant.now(clock));
        pautaRepository.save(pauta);
        log.info("Pauta cadastrada: id={}, titulo={}", pauta.getId(), pauta.getTitulo());
        return listar();
    }

    private ItemSelecao paraItem(Pauta pauta) {
        String texto = pauta.getTitulo() + " — " + statusDe(pauta);
        String url = "/api/v1/pautas/" + pauta.getId() + "/menu";
        return new ItemSelecao(texto, url);
    }

    private String statusDe(Pauta pauta) {
        return sessaoVotacaoRepository.findByPautaId(pauta.getId())
                .map(sessao -> sessao.estaAberta(clock) ? "votação aberta" : "votação encerrada")
                .orElse("aguardando abertura de sessão");
    }

}
