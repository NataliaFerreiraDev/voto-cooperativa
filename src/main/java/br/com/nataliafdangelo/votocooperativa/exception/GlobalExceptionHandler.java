package br.com.nataliafdangelo.votocooperativa.exception;

import br.com.nataliafdangelo.votocooperativa.dto.ErroResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(PautaNaoEncontradaException.class)
    public ResponseEntity<ErroResponse> tratarPautaNaoEncontrada(PautaNaoEncontradaException ex) {
        return responder(HttpStatus.NOT_FOUND, "Pauta não encontrada", ex);
    }

    @ExceptionHandler(SessaoNaoEncontradaException.class)
    public ResponseEntity<ErroResponse> tratarSessaoNaoEncontrada(SessaoNaoEncontradaException ex) {
        return responder(HttpStatus.NOT_FOUND, "Sessão não encontrada", ex);
    }

    @ExceptionHandler(SessaoJaAbertaException.class)
    public ResponseEntity<ErroResponse> tratarSessaoJaAberta(SessaoJaAbertaException ex) {
        return responder(HttpStatus.CONFLICT, "Sessão já aberta", ex);
    }

    @ExceptionHandler(SessaoFechadaException.class)
    public ResponseEntity<ErroResponse> tratarSessaoFechada(SessaoFechadaException ex) {
        return responder(HttpStatus.UNPROCESSABLE_CONTENT, "Sessão fechada", ex);
    }

    @ExceptionHandler(SessaoAindaAbertaException.class)
    public ResponseEntity<ErroResponse> tratarSessaoAindaAberta(SessaoAindaAbertaException ex) {
        return responder(HttpStatus.UNPROCESSABLE_CONTENT, "Sessão ainda aberta", ex);
    }

    @ExceptionHandler(VotoDuplicadoException.class)
    public ResponseEntity<ErroResponse> tratarVotoDuplicado(VotoDuplicadoException ex) {
        return responder(HttpStatus.CONFLICT, "Voto duplicado", ex);
    }

    @ExceptionHandler(CpfInvalidoException.class)
    public ResponseEntity<ErroResponse> tratarCpfInvalido(CpfInvalidoException ex) {
        return responder(HttpStatus.NOT_FOUND, "CPF inválido", ex);
    }

    @ExceptionHandler(AssociadoNaoAptoException.class)
    public ResponseEntity<ErroResponse> tratarAssociadoNaoApto(AssociadoNaoAptoException ex) {
        return responder(HttpStatus.FORBIDDEN, "Associado não apto a votar", ex);
    }

    @ExceptionHandler(ServicoCpfIndisponivelException.class)
    public ResponseEntity<ErroResponse> tratarServicoCpfIndisponivel(ServicoCpfIndisponivelException ex) {
        return responder(HttpStatus.SERVICE_UNAVAILABLE, "Serviço indisponível", ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarValidacao(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .orElse("Dados inválidos");
        log.warn("Validação falhou: {}", mensagem);
        return ResponseEntity.badRequest().body(new ErroResponse("Dados inválidos", mensagem));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> tratarCorpoInvalido(HttpMessageNotReadableException ex) {
        log.warn("Corpo da requisição inválido ou malformado");
        return ResponseEntity.badRequest()
                .body(new ErroResponse("Requisição inválida", "O corpo da requisição está malformado"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> tratarErroInesperado(Exception ex) {
        log.error("Erro inesperado", ex);
        return ResponseEntity.internalServerError()
                .body(new ErroResponse("Erro interno", "Ocorreu um erro inesperado. Tente novamente."));
    }

    private ResponseEntity<ErroResponse> responder(HttpStatus status, String titulo, RuntimeException ex) {
        log.warn("{}: {}", titulo, ex.getMessage());
        return ResponseEntity.status(status).body(new ErroResponse(titulo, ex.getMessage()));
    }

}
