package br.com.nataliafdangelo.votocooperativa.service;

import br.com.nataliafdangelo.votocooperativa.domain.OpcaoVoto;
import br.com.nataliafdangelo.votocooperativa.domain.Pauta;
import br.com.nataliafdangelo.votocooperativa.domain.SessaoVotacao;
import br.com.nataliafdangelo.votocooperativa.dto.TelaFormulario;
import br.com.nataliafdangelo.votocooperativa.exception.PautaNaoEncontradaException;
import br.com.nataliafdangelo.votocooperativa.exception.SessaoAindaAbertaException;
import br.com.nataliafdangelo.votocooperativa.exception.SessaoNaoEncontradaException;
import br.com.nataliafdangelo.votocooperativa.repository.PautaRepository;
import br.com.nataliafdangelo.votocooperativa.repository.SessaoVotacaoRepository;
import br.com.nataliafdangelo.votocooperativa.repository.VotoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResultadoServiceTest {

    @Mock
    private PautaRepository pautaRepository;

    @Mock
    private SessaoVotacaoRepository sessaoVotacaoRepository;

    @Mock
    private VotoRepository votoRepository;

    private final Clock clock = Clock.fixed(Instant.parse("2026-08-18T10:00:00Z"), ZoneOffset.UTC);

    private ResultadoService resultadoService;

    @BeforeEach
    void setUp() {
        resultadoService = new ResultadoService(pautaRepository, sessaoVotacaoRepository, votoRepository, clock);
    }

    private SessaoVotacao sessaoEncerrada(Pauta pauta) {
        return new SessaoVotacao(pauta, Instant.now(clock).minusSeconds(120), Instant.now(clock).minusSeconds(60));
    }

    private VotoRepository.ContagemVoto contagem(OpcaoVoto opcao, long total) {
        return new VotoRepository.ContagemVoto() {
            @Override
            public OpcaoVoto getOpcao() {
                return opcao;
            }

            @Override
            public Long getTotal() {
                return total;
            }
        };
    }

    @ParameterizedTest(name = "sim={0}, não={1} -> {2}")
    @CsvSource({
            "3, 1, Aprovada",
            "1, 3, Reprovada",
            "2, 2, Empate"
    })
    void deveDecidirResultadoConformeContagemDeVotos(long votosSim, long votosNao, String resultadoEsperado) {
        // given
        Pauta pauta = new Pauta("Pauta", "desc", Instant.now(clock));
        SessaoVotacao sessao = sessaoEncerrada(pauta);
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(sessaoVotacaoRepository.findByPautaId(1L)).thenReturn(Optional.of(sessao));
        when(votoRepository.contarPorOpcao(sessao.getId())).thenReturn(List.of(
                contagem(OpcaoVoto.SIM, votosSim),
                contagem(OpcaoVoto.NAO, votosNao)
        ));

        // when
        TelaFormulario resultado = resultadoService.resultado(1L);

        // then
        assertThat(resultado.itens().get(2).texto()).isEqualTo("Resultado: " + resultadoEsperado);
    }

    @Test
    void deveRejeitarResultadoQuandoSessaoAindaAberta() {
        // given
        Pauta pauta = new Pauta("Pauta", "desc", Instant.now(clock));
        SessaoVotacao sessao = new SessaoVotacao(pauta, Instant.now(clock), Instant.now(clock).plusSeconds(60));
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(sessaoVotacaoRepository.findByPautaId(1L)).thenReturn(Optional.of(sessao));

        // when / then
        assertThrows(SessaoAindaAbertaException.class, () -> resultadoService.resultado(1L));
    }

    @Test
    void deveRejeitarResultadoQuandoSessaoNaoExiste() {
        // given
        Pauta pauta = new Pauta("Pauta", "desc", Instant.now(clock));
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(sessaoVotacaoRepository.findByPautaId(1L)).thenReturn(Optional.empty());

        // when / then
        assertThrows(SessaoNaoEncontradaException.class, () -> resultadoService.resultado(1L));
    }

    @Test
    void deveRejeitarResultadoQuandoPautaNaoExiste() {
        // given
        when(pautaRepository.findById(1L)).thenReturn(Optional.empty());

        // when / then
        assertThrows(PautaNaoEncontradaException.class, () -> resultadoService.resultado(1L));
    }

}
