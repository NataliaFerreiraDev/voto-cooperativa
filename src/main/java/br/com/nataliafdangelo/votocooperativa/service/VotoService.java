package br.com.nataliafdangelo.votocooperativa.service;

import br.com.nataliafdangelo.votocooperativa.client.CpfEligibilidadeClient;
import br.com.nataliafdangelo.votocooperativa.client.StatusVoto;
import br.com.nataliafdangelo.votocooperativa.domain.OpcaoVoto;
import br.com.nataliafdangelo.votocooperativa.domain.SessaoVotacao;
import br.com.nataliafdangelo.votocooperativa.domain.Voto;
import br.com.nataliafdangelo.votocooperativa.exception.AssociadoNaoAptoException;
import br.com.nataliafdangelo.votocooperativa.exception.SessaoFechadaException;
import br.com.nataliafdangelo.votocooperativa.exception.SessaoNaoEncontradaException;
import br.com.nataliafdangelo.votocooperativa.exception.VotoDuplicadoException;
import br.com.nataliafdangelo.votocooperativa.repository.SessaoVotacaoRepository;
import br.com.nataliafdangelo.votocooperativa.repository.VotoRepository;
import br.com.nataliafdangelo.votocooperativa.util.Mascara;
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
    private final CpfEligibilidadeClient cpfEligibilidadeClient;
    private final Clock clock;

    public VotoService(SessaoVotacaoRepository sessaoVotacaoRepository,
                        VotoRepository votoRepository,
                        CpfEligibilidadeClient cpfEligibilidadeClient,
                        Clock clock) {
        this.sessaoVotacaoRepository = sessaoVotacaoRepository;
        this.votoRepository = votoRepository;
        this.cpfEligibilidadeClient = cpfEligibilidadeClient;
        this.clock = clock;
    }

    /**
     * Verifica se o associado pode votar. Chamada tanto na etapa de seleção de opções
     * (para falhar cedo e evitar mostrar a tela de Sim/Não a quem não pode votar) quanto
     * dentro de {@link #votar}, garantindo que a regra não possa ser contornada por uma
     * chamada direta ao endpoint de registro de voto, sem passar pela etapa anterior.
     */
    public void verificarElegibilidade(String associadoId) {
        if (cpfEligibilidadeClient.verificar(associadoId) != StatusVoto.ABLE_TO_VOTE) {
            throw new AssociadoNaoAptoException(associadoId);
        }
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

        verificarElegibilidade(associadoId);

        votoRepository.save(new Voto(sessao, associadoId, opcao, Instant.now(clock)));

        if (log.isInfoEnabled()) {
            log.info("Voto registrado: pautaId={}, associadoId={}, opcao={}", pautaId, Mascara.cpf(associadoId), opcao);
        }
    }

}
