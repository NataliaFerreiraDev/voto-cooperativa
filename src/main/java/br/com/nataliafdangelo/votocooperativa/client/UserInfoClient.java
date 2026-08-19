package br.com.nataliafdangelo.votocooperativa.client;

import br.com.nataliafdangelo.votocooperativa.exception.CpfInvalidoException;
import br.com.nataliafdangelo.votocooperativa.exception.ServicoCpfIndisponivelException;
import br.com.nataliafdangelo.votocooperativa.util.Mascara;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Integração com o serviço externo de verificação de aptidão de voto por CPF.
 * GET {baseUrl}/users/{cpf} -> 200 {"status": "ABLE_TO_VOTE" | "UNABLE_TO_VOTE"} ou 404 (CPF inválido).
 */
@Component
public class UserInfoClient implements CpfEligibilidadeClient {

    private static final Logger log = LoggerFactory.getLogger(UserInfoClient.class);

    private final RestClient restClient;

    public UserInfoClient(RestClient cpfServiceRestClient) {
        this.restClient = cpfServiceRestClient;
    }

    @Override
    public StatusVoto verificar(String cpf) {
        try {
            UsuarioResponse resposta = restClient.get()
                    .uri("/users/{cpf}", cpf)
                    .retrieve()
                    .body(UsuarioResponse.class);

            return resposta != null ? resposta.status() : StatusVoto.UNABLE_TO_VOTE;
        } catch (HttpClientErrorException.NotFound ex) {
            throw new CpfInvalidoException(cpf);
        } catch (RestClientException ex) {
            if (log.isWarnEnabled()) {
                log.warn("Falha ao consultar serviço de CPF para {}: {}", Mascara.cpf(cpf), ex.getMessage());
            }
            throw new ServicoCpfIndisponivelException(ex);
        }
    }

    private record UsuarioResponse(StatusVoto status) {
    }
}
