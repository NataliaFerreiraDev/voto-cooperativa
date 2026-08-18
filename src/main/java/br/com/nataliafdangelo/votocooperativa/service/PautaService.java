package br.com.nataliafdangelo.votocooperativa.service;

import br.com.nataliafdangelo.votocooperativa.domain.Pauta;
import br.com.nataliafdangelo.votocooperativa.domain.SessaoVotacao;
import br.com.nataliafdangelo.votocooperativa.dto.CriarPautaRequest;
import br.com.nataliafdangelo.votocooperativa.dto.ItemSelecao;
import br.com.nataliafdangelo.votocooperativa.dto.TelaSelecao;
import br.com.nataliafdangelo.votocooperativa.exception.PautaNaoEncontradaException;
import br.com.nataliafdangelo.votocooperativa.repository.PautaRepository;
import br.com.nataliafdangelo.votocooperativa.repository.SessaoVotacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PautaService {

    private static final Logger log = LoggerFactory.getLogger(PautaService.class);
    private static final String BASE_URL_PAUTAS = "/api/v1/pautas/";

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
        String url = BASE_URL_PAUTAS + pauta.getId() + "/menu";
        return new ItemSelecao(texto, url);
    }

    private String statusDe(Pauta pauta) {
        return sessaoVotacaoRepository.findByPautaId(pauta.getId())
                .map(sessao -> sessao.estaAberta(clock) ? "votação aberta" : "votação encerrada")
                .orElse("aguardando abertura de sessão");
    }

    public TelaSelecao menu(Long pautaId) {
        Pauta pauta = pautaRepository.findById(pautaId)
                .orElseThrow(() -> new PautaNaoEncontradaException(pautaId));

        List<ItemSelecao> itens = new ArrayList<>();
        Optional<SessaoVotacao> sessao = sessaoVotacaoRepository.findByPautaId(pautaId);

        if (sessao.isEmpty()) {
            itens.add(new ItemSelecao("Abrir sessão de votação",
                    BASE_URL_PAUTAS + pautaId + "/sessoes/novo"));
        } else if (sessao.get().estaAberta(clock)) {
            itens.add(new ItemSelecao("Votar",
                    BASE_URL_PAUTAS + pautaId + "/votos/novo"));
        }
        // Fase 7 adiciona aqui: "Ver resultado" quando a sessão estiver encerrada

        return new TelaSelecao(pauta.getTitulo(), itens);
    }

}
