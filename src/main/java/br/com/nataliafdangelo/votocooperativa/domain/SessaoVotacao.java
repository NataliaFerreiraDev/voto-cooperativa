package br.com.nataliafdangelo.votocooperativa.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.Clock;
import java.time.Instant;

@Entity
@Table(name = "sessao_votacao")
public class SessaoVotacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pauta_id", nullable = false, unique = true)
    private Pauta pauta;

    @Column(nullable = false)
    private Instant dataAbertura;

    @Column(nullable = false)
    private Instant dataFechamento;

    protected SessaoVotacao() {
    }

    public SessaoVotacao(Pauta pauta, Instant dataAbertura, Instant dataFechamento) {
        this.pauta = pauta;
        this.dataAbertura = dataAbertura;
        this.dataFechamento = dataFechamento;
    }

    public boolean estaAberta(Clock clock) {
        return Instant.now(clock).isBefore(dataFechamento);
    }

    public Long getId() {
        return id;
    }

    public Pauta getPauta() {
        return pauta;
    }

    public Instant getDataAbertura() {
        return dataAbertura;
    }

    public Instant getDataFechamento() {
        return dataFechamento;
    }

}
