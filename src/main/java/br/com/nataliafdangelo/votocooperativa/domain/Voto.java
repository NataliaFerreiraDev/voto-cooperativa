package br.com.nataliafdangelo.votocooperativa.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
        name = "voto",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_voto_sessao_associado",
                columnNames = {"sessao_votacao_id", "associado_id"}
        )
)
public class Voto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sessao_votacao_id", nullable = false)
    private SessaoVotacao sessaoVotacao;

    @Column(name = "associado_id", nullable = false)
    private String associadoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OpcaoVoto opcao;

    @Column(nullable = false)
    private Instant dataVoto;

    protected Voto() {
    }

    public Voto(SessaoVotacao sessaoVotacao, String associadoId, OpcaoVoto opcao, Instant dataVoto) {
        this.sessaoVotacao = sessaoVotacao;
        this.associadoId = associadoId;
        this.opcao = opcao;
        this.dataVoto = dataVoto;
    }

    public Long getId() {
        return id;
    }

    public SessaoVotacao getSessaoVotacao() {
        return sessaoVotacao;
    }

    public String getAssociadoId() {
        return associadoId;
    }

    public OpcaoVoto getOpcao() {
        return opcao;
    }

    public Instant getDataVoto() {
        return dataVoto;
    }

}
