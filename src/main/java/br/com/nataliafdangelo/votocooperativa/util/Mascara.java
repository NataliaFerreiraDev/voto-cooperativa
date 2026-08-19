package br.com.nataliafdangelo.votocooperativa.util;

/**
 * Utilitário para mascarar dados sensíveis em logs (ex: CPF), evitando expor
 * o dado completo mesmo em nível INFO/WARN.
 */
public final class Mascara {

    private Mascara() {
    }

    public static String cpf(String cpf) {
        if (cpf == null || cpf.length() < 4) {
            return "***";
        }
        String ultimosDigitos = cpf.substring(cpf.length() - 2);
        return "*".repeat(cpf.length() - 2) + ultimosDigitos;
    }
}
