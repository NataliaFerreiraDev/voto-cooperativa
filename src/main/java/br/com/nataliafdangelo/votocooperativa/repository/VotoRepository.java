package br.com.nataliafdangelo.votocooperativa.repository;

import br.com.nataliafdangelo.votocooperativa.domain.Voto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VotoRepository extends JpaRepository<Voto, Long> {

    boolean existsBySessaoVotacaoIdAndAssociadoId(Long sessaoVotacaoId, String associadoId);

}
