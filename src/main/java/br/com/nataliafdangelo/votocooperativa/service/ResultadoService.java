package br.com.nataliafdangelo.votocooperativa.service;

import br.com.nataliafdangelo.votocooperativa.domain.OpcaoVoto;
import br.com.nataliafdangelo.votocooperativa.domain.Pauta;
import br.com.nataliafdangelo.votocooperativa.domain.SessaoVotacao;
import br.com.nataliafdangelo.votocooperativa.dto.Botao;
import br.com.nataliafdangelo.votocooperativa.dto.ItemFormulario;
import br.com.nataliafdangelo.votocooperativa.dto.TelaFormulario;
import br.com.nataliafdangelo.votocooperativa.exception.PautaNaoEncontradaException;
import br.com.nataliafdangelo.votocooperativa.exception.SessaoAindaAbertaException;
import br.com.nataliafdangelo.votocooperativa.exception.SessaoNaoEncontradaException;
import br.com.nataliafdangelo.votocooperativa.repository.PautaRepository;
import br.com.nataliafdangelo.votocooperativa.repository.SessaoVotacaoRepository;
import br.com.nataliafdangelo.votocooperativa.repository.VotoRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ResultadoService {

    private final PautaRepository pautaRepository;
    private final SessaoVotacaoRepository sessaoVotacaoRepository;
    private final VotoRepository votoRepository;
    private final Clock clock;

    public ResultadoService(PautaRepository pautaRepository,
                             SessaoVotacaoRepository sessaoVotacaoRepository,
                             VotoRepository votoRepository,
                             Clock clock) {
        this.pautaRepository = pautaRepository;
        this.sessaoVotacaoRepository = sessaoVotacaoRepository;
        this.votoRepository = votoRepository;
        this.clock = clock;
    }

    public TelaFormulario resultado(Long pautaId) {
        Pauta pauta = pautaRepository.findById(pautaId)
                .orElseThrow(() -> new PautaNaoEncontradaException(pautaId));

        SessaoVotacao sessao = sessaoVotacaoRepository.findByPautaId(pautaId)
                .orElseThrow(() -> new SessaoNaoEncontradaException(pautaId));

        if (sessao.estaAberta(clock)) {
            throw new SessaoAindaAbertaException(pautaId);
        }

        Map<OpcaoVoto, Long> contagem = contarVotos(sessao.getId());
        long votosSim = contagem.getOrDefault(OpcaoVoto.SIM, 0L);
        long votosNao = contagem.getOrDefault(OpcaoVoto.NAO, 0L);

        List<ItemFormulario> itens = List.of(
                ItemFormulario.texto("Sim: " + votosSim + " voto(s)"),
                ItemFormulario.texto("Não: " + votosNao + " voto(s)"),
                ItemFormulario.texto("Resultado: " + decidirResultado(votosSim, votosNao))
        );

        return new TelaFormulario(pauta.getTitulo(), itens,
                new Botao("Voltar", "/api/v1/pautas/lista", Map.of()));
    }

    private Map<OpcaoVoto, Long> contarVotos(Long sessaoVotacaoId) {
        return votoRepository.contarPorOpcao(sessaoVotacaoId).stream()
                .collect(Collectors.toMap(VotoRepository.ContagemVoto::getOpcao,
                        VotoRepository.ContagemVoto::getTotal));
    }

    private String decidirResultado(long votosSim, long votosNao) {
        if (votosSim > votosNao) {
            return "Aprovada";
        }
        if (votosSim < votosNao) {
            return "Reprovada";
        }
        return "Empate";
    }
}
