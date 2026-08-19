package br.com.nataliafdangelo.votocooperativa.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ServicoCpfIndisponivelException extends RuntimeException {
    public ServicoCpfIndisponivelException(Throwable causa) {
        super("Serviço de validação de CPF indisponível no momento. Tente novamente em instantes.", causa);
    }
}
