package br.com.nataliafdangelo.votocooperativa.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "Voto Cooperativa API",
                version = "v1",
                description = "API REST para gestão de pautas e votação em assembleias de cooperativa. " +
                        "Respostas seguem o contrato de tela do Anexo 1 (FORMULARIO/SELECAO)."
        )
)
@Configuration
public class OpenApiConfig {
}
