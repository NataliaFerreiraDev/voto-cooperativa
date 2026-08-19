package br.com.nataliafdangelo.votocooperativa.service;

import br.com.nataliafdangelo.votocooperativa.client.CpfEligibilidadeClient;
import br.com.nataliafdangelo.votocooperativa.client.StatusVoto;
import br.com.nataliafdangelo.votocooperativa.domain.OpcaoVoto;
import br.com.nataliafdangelo.votocooperativa.domain.Pauta;
import br.com.nataliafdangelo.votocooperativa.domain.SessaoVotacao;
import br.com.nataliafdangelo.votocooperativa.domain.Voto;
import br.com.nataliafdangelo.votocooperativa.exception.AssociadoNaoAptoException;
import br.com.nataliafdangelo.votocooperativa.exception.SessaoFechadaException;
import br.com.nataliafdangelo.votocooperativa.exception.SessaoNaoEncontradaException;
import br.com.nataliafdangelo.votocooperativa.exception.VotoDuplicadoException;
import br.com.nataliafdangelo.votocooperativa.repository.SessaoVotacaoRepository;
import br.com.nataliafdangelo.votocooperativa.repository.VotoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VotoServiceTest {

    @Mock
    private SessaoVotacaoRepository sessaoVotacaoRepository;

    @Mock
    private VotoRepository votoRepository;

    @Mock
    private CpfEligibilidadeClient cpfEligibilidadeClient;

    private final Clock clock = Clock.fixed(Instant.parse("2026-08-18T10:00:00Z"), ZoneOffset.UTC);

    private VotoService votoService;

    @BeforeEach
    void setUp() {
        votoService = new VotoService(sessaoVotacaoRepository, votoRepository, cpfEligibilidadeClient, clock);
    }

    @Test
    void deveRegistrarVotoComSucesso() {
        // given
        Pauta pauta = new Pauta("Pauta", "desc", Instant.now(clock));
        SessaoVotacao sessao = new SessaoVotacao(pauta, Instant.now(clock), Instant.now(clock).plusSeconds(60));
        when(sessaoVotacaoRepository.findByPautaId(1L)).thenReturn(Optional.of(sessao));
        when(votoRepository.existsBySessaoVotacaoIdAndAssociadoId(sessao.getId(), "12345678900"))
                .thenReturn(false);
        when(cpfEligibilidadeClient.verificar("12345678900")).thenReturn(StatusVoto.ABLE_TO_VOTE);

        // when
        votoService.votar(1L, "12345678900", OpcaoVoto.SIM);

        // then
        ArgumentCaptor<Voto> captor = ArgumentCaptor.forClass(Voto.class);
        verify(votoRepository).save(captor.capture());
        Voto salvo = captor.getValue();
        assertThat(salvo.getAssociadoId()).isEqualTo("12345678900");
        assertThat(salvo.getOpcao()).isEqualTo(OpcaoVoto.SIM);
    }

    @Test
    void deveRejeitarVotoQuandoSessaoNaoExiste() {
        // given
        when(sessaoVotacaoRepository.findByPautaId(1L)).thenReturn(Optional.empty());

        // when / then
        assertThrows(SessaoNaoEncontradaException.class,
                () -> votoService.votar(1L, "12345678900", OpcaoVoto.SIM));
    }

    @Test
    void deveRejeitarVotoQuandoSessaoEstaFechada() {
        // given
        Pauta pauta = new Pauta("Pauta", "desc", Instant.now(clock));
        SessaoVotacao sessao = new SessaoVotacao(pauta,
                Instant.now(clock).minusSeconds(120), Instant.now(clock).minusSeconds(60));
        when(sessaoVotacaoRepository.findByPautaId(1L)).thenReturn(Optional.of(sessao));

        // when / then
        assertThrows(SessaoFechadaException.class,
                () -> votoService.votar(1L, "12345678900", OpcaoVoto.SIM));
    }

    @Test
    void deveRejeitarVotoDuplicado() {
        // given
        Pauta pauta = new Pauta("Pauta", "desc", Instant.now(clock));
        SessaoVotacao sessao = new SessaoVotacao(pauta, Instant.now(clock), Instant.now(clock).plusSeconds(60));
        when(sessaoVotacaoRepository.findByPautaId(1L)).thenReturn(Optional.of(sessao));
        when(votoRepository.existsBySessaoVotacaoIdAndAssociadoId(sessao.getId(), "12345678900"))
                .thenReturn(true);

        // when / then
        assertThrows(VotoDuplicadoException.class,
                () -> votoService.votar(1L, "12345678900", OpcaoVoto.SIM));
    }

    @Test
    void deveRejeitarVotoQuandoAssociadoNaoApto() {
        // given
        Pauta pauta = new Pauta("Pauta", "desc", Instant.now(clock));
        SessaoVotacao sessao = new SessaoVotacao(pauta, Instant.now(clock), Instant.now(clock).plusSeconds(60));
        when(sessaoVotacaoRepository.findByPautaId(1L)).thenReturn(Optional.of(sessao));
        when(votoRepository.existsBySessaoVotacaoIdAndAssociadoId(sessao.getId(), "12345678900"))
                .thenReturn(false);
        when(cpfEligibilidadeClient.verificar("12345678900")).thenReturn(StatusVoto.UNABLE_TO_VOTE);

        // when / then
        assertThrows(AssociadoNaoAptoException.class,
                () -> votoService.votar(1L, "12345678900", OpcaoVoto.SIM));
        verify(votoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void deveVerificarElegibilidadeDiretamente() {
        // given
        when(cpfEligibilidadeClient.verificar("12345678900")).thenReturn(StatusVoto.UNABLE_TO_VOTE);

        // when / then
        assertThrows(AssociadoNaoAptoException.class,
                () -> votoService.verificarElegibilidade("12345678900"));
    }

}
