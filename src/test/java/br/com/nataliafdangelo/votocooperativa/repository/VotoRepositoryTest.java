package br.com.nataliafdangelo.votocooperativa.repository;

import br.com.nataliafdangelo.votocooperativa.domain.OpcaoVoto;
import br.com.nataliafdangelo.votocooperativa.domain.Pauta;
import br.com.nataliafdangelo.votocooperativa.domain.SessaoVotacao;
import br.com.nataliafdangelo.votocooperativa.domain.Voto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class VotoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VotoRepository votoRepository;

    @Test
    void deveRejeitarVotoDuplicadoDoMesmoAssociadoNaMesmaSessao() {
        // given
        Pauta pauta = entityManager.persistAndFlush(
                new Pauta("Pauta de teste", "descrição", Instant.now()));
        SessaoVotacao sessao = entityManager.persistAndFlush(
                new SessaoVotacao(pauta, Instant.now(), Instant.now().plusSeconds(60)));
        votoRepository.saveAndFlush(new Voto(sessao, "12345678900", OpcaoVoto.SIM, Instant.now()));

        Voto duplicado = new Voto(sessao, "12345678900", OpcaoVoto.NAO, Instant.now());

        // when / then
        assertThrows(DataIntegrityViolationException.class,
                () -> votoRepository.saveAndFlush(duplicado));
    }

}
