package br.com.nataliafdangelo.votocooperativa.exception;

import br.com.nataliafdangelo.votocooperativa.util.Mascara;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class AssociadoNaoAptoException extends RuntimeException {
    public AssociadoNaoAptoException(String cpf) {
        super("Associado " + Mascara.cpf(cpf) + " não está apto a votar");
    }
}
