package br.com.nataliafdangelo.votocooperativa.client;

import br.com.nataliafdangelo.votocooperativa.exception.CpfInvalidoException;
import br.com.nataliafdangelo.votocooperativa.exception.ServicoCpfIndisponivelException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_FOUND;

class UserInfoClientTest {

    private static final String BASE_URL = "https://user-info.herokuapp.com";

    private MockRestServiceServer mockServer;
    private UserInfoClient client;

    private void configurar() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new UserInfoClient(builder.build());
    }

    @Test
    void deveRetornarAbleToVoteQuandoCpfValidoEApto() {
        configurar();
        mockServer.expect(requestTo(BASE_URL + "/users/12345678900"))
                .andExpect(method(GET))
                .andRespond(withSuccess("{\"status\": \"ABLE_TO_VOTE\"}", MediaType.APPLICATION_JSON));

        StatusVoto status = client.verificar("12345678900");

        assertThat(status).isEqualTo(StatusVoto.ABLE_TO_VOTE);
        mockServer.verify();
    }

    @Test
    void deveRetornarUnableToVoteQuandoCpfValidoENaoApto() {
        configurar();
        mockServer.expect(requestTo(BASE_URL + "/users/62289608068"))
                .andExpect(method(GET))
                .andRespond(withSuccess("{\"status\": \"UNABLE_TO_VOTE\"}", MediaType.APPLICATION_JSON));

        StatusVoto status = client.verificar("62289608068");

        assertThat(status).isEqualTo(StatusVoto.UNABLE_TO_VOTE);
        mockServer.verify();
    }

    @Test
    void deveLancarCpfInvalidoQuandoServicoRetorna404() {
        configurar();
        mockServer.expect(requestTo(BASE_URL + "/users/00000000000"))
                .andExpect(method(GET))
                .andRespond(withStatus(NOT_FOUND));

        assertThrows(CpfInvalidoException.class, () -> client.verificar("00000000000"));
        mockServer.verify();
    }

    @Test
    void deveLancarServicoIndisponivelQuandoRespostaEInvalida() {
        configurar();
        mockServer.expect(requestTo(BASE_URL + "/users/12345678900"))
                .andExpect(method(GET))
                .andRespond(withStatus(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(ServicoCpfIndisponivelException.class, () -> client.verificar("12345678900"));
        mockServer.verify();
    }

}
