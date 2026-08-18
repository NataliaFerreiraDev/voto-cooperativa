package br.com.nataliafdangelo.votocooperativa.service;

import br.com.nataliafdangelo.votocooperativa.domain.Pauta;
import br.com.nataliafdangelo.votocooperativa.domain.SessaoVotacao;
import br.com.nataliafdangelo.votocooperativa.dto.AbrirSessaoRequest;
import br.com.nataliafdangelo.votocooperativa.exception.PautaNaoEncontradaException;
import br.com.nataliafdangelo.votocooperativa.exception.SessaoJaAbertaException;
import br.com.nataliafdangelo.votocooperativa.repository.PautaRepository;
import br.com.nataliafdangelo.votocooperativa.repository.SessaoVotacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class SessaoVotacaoService {

    private static final Logger log = LoggerFactory.getLogger(SessaoVotacaoService.class);
    private static final int DURACAO_PADRAO_MINUTOS = 1;

    private final PautaRepository pautaRepository;
    private final SessaoVotacaoRepository sessaoVotacaoRepository;
    private final Clock clock;

    public SessaoVotacaoService(PautaRepository pautaRepository,
                                SessaoVotacaoRepository sessaoVotacaoRepository,
                                Clock clock) {
        this.pautaRepository = pautaRepository;
        this.sessaoVotacaoRepository = sessaoVotacaoRepository;
        this.clock = clock;
    }

    public void abrir(Long pautaId, AbrirSessaoRequest request) {
        Pauta pauta = pautaRepository.findById(pautaId)
                .orElseThrow(() -> new PautaNaoEncontradaException(pautaId));

        if (sessaoVotacaoRepository.findByPautaId(pautaId).isPresent()) {
            throw new SessaoJaAbertaException(pautaId);
        }

        int duracaoMinutos = request.duracaoMinutos() != null
                ? request.duracaoMinutos()
                : DURACAO_PADRAO_MINUTOS;

        Instant abertura = Instant.now(clock);
        Instant fechamento = abertura.plus(Duration.ofMinutes(duracaoMinutos));

        sessaoVotacaoRepository.save(new SessaoVotacao(pauta, abertura, fechamento));

        log.info("Sessão de votação aberta: pautaId={}, duracaoMinutos={}, fechamento={}",
                pautaId, duracaoMinutos, fechamento);
    }

}
