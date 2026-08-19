package br.com.nataliafdangelo.votocooperativa.exception;

import br.com.nataliafdangelo.votocooperativa.util.Mascara;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class VotoDuplicadoException extends RuntimeException {
    public VotoDuplicadoException(Long pautaId, String associadoId) {
        super("Associado " + Mascara.cpf(associadoId) + " já votou na pauta " + pautaId);
    }
}
