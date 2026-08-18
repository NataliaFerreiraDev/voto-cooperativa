package br.com.nataliafdangelo.votocooperativa.repository;

import br.com.nataliafdangelo.votocooperativa.domain.Pauta;
import br.com.nataliafdangelo.votocooperativa.domain.SessaoVotacao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class SessaoVotacaoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SessaoVotacaoRepository sessaoVotacaoRepository;

    @Test
    void deveRejeitarSegundaSessaoParaMesmaPauta() {
        // given
        Pauta pauta = entityManager.persistAndFlush(
                new Pauta("Pauta de teste", "descrição", Instant.now()));
        sessaoVotacaoRepository.saveAndFlush(
                new SessaoVotacao(pauta, Instant.now(), Instant.now().plusSeconds(60)));

        SessaoVotacao segunda = new SessaoVotacao(pauta, Instant.now(), Instant.now().plusSeconds(120));

        // when / then
        assertThrows(DataIntegrityViolationException.class,
                () -> sessaoVotacaoRepository.saveAndFlush(segunda));
    }

}
