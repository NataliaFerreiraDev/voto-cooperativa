package br.com.nataliafdangelo.votocooperativa.service;

import br.com.nataliafdangelo.votocooperativa.domain.OpcaoVoto;
import br.com.nataliafdangelo.votocooperativa.domain.SessaoVotacao;
import br.com.nataliafdangelo.votocooperativa.domain.Voto;
import br.com.nataliafdangelo.votocooperativa.exception.SessaoFechadaException;
import br.com.nataliafdangelo.votocooperativa.exception.SessaoNaoEncontradaException;
import br.com.nataliafdangelo.votocooperativa.exception.VotoDuplicadoException;
import br.com.nataliafdangelo.votocooperativa.repository.SessaoVotacaoRepository;
import br.com.nataliafdangelo.votocooperativa.repository.VotoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
public class VotoService {

    private static final Logger log = LoggerFactory.getLogger(VotoService.class);

    private final SessaoVotacaoRepository sessaoVotacaoRepository;
    private final VotoRepository votoRepository;
    private final Clock clock;

    public VotoService(SessaoVotacaoRepository sessaoVotacaoRepository,
                        VotoRepository votoRepository,
                        Clock clock) {
        this.sessaoVotacaoRepository = sessaoVotacaoRepository;
        this.votoRepository = votoRepository;
        this.clock = clock;
    }

    public void votar(Long pautaId, String associadoId, OpcaoVoto opcao) {
        SessaoVotacao sessao = sessaoVotacaoRepository.findByPautaId(pautaId)
                .orElseThrow(() -> new SessaoNaoEncontradaException(pautaId));

        if (!sessao.estaAberta(clock)) {
            throw new SessaoFechadaException(pautaId);
        }

        if (votoRepository.existsBySessaoVotacaoIdAndAssociadoId(sessao.getId(), associadoId)) {
            throw new VotoDuplicadoException(pautaId, associadoId);
        }

        votoRepository.save(new Voto(sessao, associadoId, opcao, Instant.now(clock)));

        log.info("Voto registrado: pautaId={}, associadoId={}, opcao={}", pautaId, associadoId, opcao);
    }

}
