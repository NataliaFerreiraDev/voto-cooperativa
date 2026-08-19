package br.com.nataliafdangelo.votocooperativa.client;

/**
 * Verifica, a partir do CPF do associado, se ele pode votar.
 * Isolado em interface para permitir mock em testes, sem depender do serviço externo real.
 */
public interface CpfEligibilidadeClient {

    StatusVoto verificar(String cpf);
}
