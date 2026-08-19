package br.com.nataliafdangelo.votocooperativa.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
public class SessaoAindaAbertaException extends RuntimeException {
    public SessaoAindaAbertaException(Long pautaId) {
        super("A sessão de votação da pauta " + pautaId + " ainda está aberta");
    }
}
