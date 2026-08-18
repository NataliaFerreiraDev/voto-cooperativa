package br.com.nataliafdangelo.votocooperativa.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class VotoDuplicadoException extends RuntimeException {
    public VotoDuplicadoException(Long pautaId, String associadoId) {
        super("Associado " + associadoId + " já votou na pauta " + pautaId);
    }
}
