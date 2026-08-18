package br.com.nataliafdangelo.votocooperativa.repository;

import br.com.nataliafdangelo.votocooperativa.domain.SessaoVotacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessaoVotacaoRepository extends JpaRepository<SessaoVotacao, Long> {

    Optional<SessaoVotacao> findByPautaId(Long pautaId);

}
