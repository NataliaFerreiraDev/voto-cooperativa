package br.com.nataliafdangelo.votocooperativa.repository;

import br.com.nataliafdangelo.votocooperativa.domain.OpcaoVoto;
import br.com.nataliafdangelo.votocooperativa.domain.Voto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VotoRepository extends JpaRepository<Voto, Long> {

    boolean existsBySessaoVotacaoIdAndAssociadoId(Long sessaoVotacaoId, String associadoId);

    @Query("SELECT v.opcao AS opcao, COUNT(v) AS total FROM Voto v " +
            "WHERE v.sessaoVotacao.id = :sessaoVotacaoId GROUP BY v.opcao")
    List<ContagemVoto> contarPorOpcao(@Param("sessaoVotacaoId") Long sessaoVotacaoId);

    interface ContagemVoto {
        OpcaoVoto getOpcao();
        Long getTotal();
    }
}
