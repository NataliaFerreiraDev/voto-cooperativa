package br.com.nataliafdangelo.votocooperativa.service;

import br.com.nataliafdangelo.votocooperativa.domain.Pauta;
import br.com.nataliafdangelo.votocooperativa.domain.SessaoVotacao;
import br.com.nataliafdangelo.votocooperativa.dto.AbrirSessaoRequest;
import br.com.nataliafdangelo.votocooperativa.exception.PautaNaoEncontradaException;
import br.com.nataliafdangelo.votocooperativa.exception.SessaoJaAbertaException;
import br.com.nataliafdangelo.votocooperativa.repository.PautaRepository;
import br.com.nataliafdangelo.votocooperativa.repository.SessaoVotacaoRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessaoVotacaoServiceTest {

    @Mock
    private PautaRepository pautaRepository;

    @Mock
    private SessaoVotacaoRepository sessaoVotacaoRepository;

    private final Clock clock = Clock.fixed(Instant.parse("2026-08-18T10:00:00Z"), ZoneOffset.UTC);

    private SessaoVotacaoService sessaoVotacaoService;

    @BeforeEach
    void setUp() {
        sessaoVotacaoService = new SessaoVotacaoService(pautaRepository, sessaoVotacaoRepository, clock);
    }

    @Test
    void deveAbrirSessaoComDuracaoPadraoQuandoNaoInformada() {
        // given
        Pauta pauta = new Pauta("Pauta", "desc", Instant.now(clock));
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(sessaoVotacaoRepository.findByPautaId(1L)).thenReturn(Optional.empty());

        // when
        sessaoVotacaoService.abrir(1L, new AbrirSessaoRequest(null));

        // then
        ArgumentCaptor<SessaoVotacao> captor = ArgumentCaptor.forClass(SessaoVotacao.class);
        verify(sessaoVotacaoRepository).save(captor.capture());
        SessaoVotacao salva = captor.getValue();
        assertThat(salva.getDataFechamento()).isEqualTo(salva.getDataAbertura().plusSeconds(60));
    }

    @Test
    void deveAbrirSessaoComDuracaoInformada() {
        // given
        Pauta pauta = new Pauta("Pauta", "desc", Instant.now(clock));
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(sessaoVotacaoRepository.findByPautaId(1L)).thenReturn(Optional.empty());

        // when
        sessaoVotacaoService.abrir(1L, new AbrirSessaoRequest(5));

        // then
        ArgumentCaptor<SessaoVotacao> captor = ArgumentCaptor.forClass(SessaoVotacao.class);
        verify(sessaoVotacaoRepository).save(captor.capture());
        SessaoVotacao salva = captor.getValue();
        assertThat(salva.getDataFechamento()).isEqualTo(salva.getDataAbertura().plusSeconds(300));
    }

    @Test
    void deveRejeitarAberturaQuandoJaExisteSessao() {
        // given
        Pauta pauta = new Pauta("Pauta", "desc", Instant.now(clock));
        SessaoVotacao existente = new SessaoVotacao(pauta, Instant.now(clock), Instant.now(clock).plusSeconds(60));
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(sessaoVotacaoRepository.findByPautaId(1L)).thenReturn(Optional.of(existente));
        AbrirSessaoRequest request = new AbrirSessaoRequest(null);

        // when / then
        assertThrows(SessaoJaAbertaException.class, () -> sessaoVotacaoService.abrir(1L, request));
    }

    @Test
    void deveRejeitarAberturaQuandoPautaNaoExiste() {
        // given
        when(pautaRepository.findById(1L)).thenReturn(Optional.empty());
        AbrirSessaoRequest request = new AbrirSessaoRequest(null);

        // when / then
        assertThrows(PautaNaoEncontradaException.class, () -> sessaoVotacaoService.abrir(1L, request));
    }

}
