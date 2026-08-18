package br.com.nataliafdangelo.votocooperativa.service;

import br.com.nataliafdangelo.votocooperativa.domain.Pauta;
import br.com.nataliafdangelo.votocooperativa.domain.SessaoVotacao;
import br.com.nataliafdangelo.votocooperativa.dto.CriarPautaRequest;
import br.com.nataliafdangelo.votocooperativa.dto.TelaSelecao;
import br.com.nataliafdangelo.votocooperativa.exception.PautaNaoEncontradaException;
import br.com.nataliafdangelo.votocooperativa.repository.PautaRepository;
import br.com.nataliafdangelo.votocooperativa.repository.SessaoVotacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PautaServiceTest {

    @Mock
    private PautaRepository pautaRepository;

    @Mock
    private SessaoVotacaoRepository sessaoVotacaoRepository;

    private final Clock clock = Clock.fixed(Instant.parse("2026-08-18T10:00:00Z"), ZoneOffset.UTC);

    private PautaService pautaService;

    @BeforeEach
    void setUp() {
        pautaService = new PautaService(pautaRepository, sessaoVotacaoRepository, clock);
    }

    @Test
    void deveCriarPautaERetornarListaAtualizadaComStatus() {
        // given
        CriarPautaRequest request = new CriarPautaRequest("Reforma do estatuto", "descrição");
        Pauta pautaSalva = new Pauta("Reforma do estatuto", "descrição", Instant.now(clock));
        when(pautaRepository.findAll()).thenReturn(List.of(pautaSalva));
        when(sessaoVotacaoRepository.findByPautaId(pautaSalva.getId())).thenReturn(Optional.empty());

        // when
        TelaSelecao resultado = pautaService.criar(request);

        // then
        verify(pautaRepository).save(any(Pauta.class));
        assertThat(resultado.itens()).hasSize(1);
        assertThat(resultado.itens().getFirst().texto())
                .contains("Reforma do estatuto")
                .contains("aguardando abertura de sessão");
    }

    @Test
    void deveListarPautaComSessaoAberta() {
        // given
        Pauta pauta = new Pauta("Pauta com sessão aberta", "desc", Instant.now(clock));
        SessaoVotacao sessaoAberta = new SessaoVotacao(pauta, Instant.now(clock), Instant.now(clock).plusSeconds(60));
        when(pautaRepository.findAll()).thenReturn(List.of(pauta));
        when(sessaoVotacaoRepository.findByPautaId(pauta.getId())).thenReturn(Optional.of(sessaoAberta));

        // when
        TelaSelecao resultado = pautaService.listar();

        // then
        assertThat(resultado.itens().getFirst().texto()).contains("votação aberta");
    }

    @Test
    void deveListarPautaComSessaoEncerrada() {
        // given
        Pauta pauta = new Pauta("Pauta com sessão encerrada", "desc", Instant.now(clock));
        SessaoVotacao sessaoEncerrada = new SessaoVotacao(pauta,
                Instant.now(clock).minusSeconds(120), Instant.now(clock).minusSeconds(60));
        when(pautaRepository.findAll()).thenReturn(List.of(pauta));
        when(sessaoVotacaoRepository.findByPautaId(pauta.getId())).thenReturn(Optional.of(sessaoEncerrada));

        // when
        TelaSelecao resultado = pautaService.listar();

        // then
        assertThat(resultado.itens().getFirst().texto()).contains("votação encerrada");
    }

    @Test
    void deveRetornarMenuComOpcaoDeAbrirSessaoQuandoNaoExisteSessao() {
        // given
        Pauta pauta = new Pauta("Pauta sem sessão", "desc", Instant.now(clock));
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(sessaoVotacaoRepository.findByPautaId(1L)).thenReturn(Optional.empty());

        // when
        TelaSelecao menu = pautaService.menu(1L);

        // then
        assertThat(menu.itens()).hasSize(1);
        assertThat(menu.itens().getFirst().texto()).isEqualTo("Abrir sessão de votação");
    }

    @Test
    void deveRetornarMenuComOpcaoDeVotarQuandoSessaoEstaAberta() {
        // given
        Pauta pauta = new Pauta("Pauta com sessão aberta", "desc", Instant.now(clock));
        SessaoVotacao sessao = new SessaoVotacao(pauta, Instant.now(clock), Instant.now(clock).plusSeconds(60));
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(sessaoVotacaoRepository.findByPautaId(1L)).thenReturn(Optional.of(sessao));

        // when
        TelaSelecao menu = pautaService.menu(1L);

        // then
        assertThat(menu.itens()).hasSize(1);
        assertThat(menu.itens().getFirst().texto()).isEqualTo("Votar");
    }

    @Test
    void deveRetornarMenuSemOpcoesQuandoSessaoEstaEncerrada() {
        // given
        Pauta pauta = new Pauta("Pauta com sessão encerrada", "desc", Instant.now(clock));
        SessaoVotacao sessao = new SessaoVotacao(pauta,
                Instant.now(clock).minusSeconds(120), Instant.now(clock).minusSeconds(60));
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(sessaoVotacaoRepository.findByPautaId(1L)).thenReturn(Optional.of(sessao));

        // when
        TelaSelecao menu = pautaService.menu(1L);

        // then
        assertThat(menu.itens()).isEmpty();
    }

    @Test
    void deveLancarExcecaoQuandoPautaDoMenuNaoExiste() {
        // given
        when(pautaRepository.findById(1L)).thenReturn(Optional.empty());

        // when / then
        assertThrows(PautaNaoEncontradaException.class, () -> pautaService.menu(1L));
    }

}
