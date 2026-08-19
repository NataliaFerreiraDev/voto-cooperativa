# Voto Cooperativa

API REST para gestão de pautas e sessões de votação em assembleias de cooperativa, desenvolvida como avaliação técnica. Cada associado tem um voto (Sim/Não) por pauta. A comunicação com o cliente mobile segue um contrato de tela *server-driven* (FORMULARIO/SELECAO), detalhado no Anexo 1 do desafio.

## Sumário

- [Como executar](#como-executar)
- [Stack e principais decisões técnicas](#stack-e-principais-decisões-técnicas)
- [Arquitetura](#arquitetura)
- [Contrato de tela (Anexo 1)](#contrato-de-tela-anexo-1)
- [Fluxo de endpoints](#fluxo-de-endpoints)
- [Persistência](#persistência)
- [Tratamento de erros](#tratamento-de-erros)
- [Bônus 1 — Integração com CPF](#bônus-1--integração-com-cpf)
- [Bônus 2 — Performance](#bônus-2--performance)
- [Bônus 3 — Versionamento da API](#bônus-3--versionamento-da-api)
- [Testes e qualidade](#testes-e-qualidade)
- [Docker](#docker)
- [Assunções e decisões documentadas](#assunções-e-decisões-documentadas)

## Como executar

**Requisito único: JDK 21.** Não é necessário ter Maven instalado (o projeto inclui o Maven Wrapper) nem Docker — a aplicação sobe com um único comando, sem dependência externa.

```bash
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`. Documentação interativa (Swagger UI) em `http://localhost:8080/swagger-ui.html`.

O banco de dados é H2 persistido em **arquivo** (`./data/`, criado automaticamente na raiz do projeto), não em memória, garantindo que pautas e votos não se percam com o restart da aplicação, como exigido no enunciado.

### Rodando com Postgres (opcional)

```bash
docker compose up
```

Sobe a aplicação com profile `postgres` mais um container Postgres. O H2 continua sendo o caminho padrão e garantido de execução. O Postgres é oferecido como alternativa mais próxima de produção.

## Stack e principais decisões técnicas

- **Linguagem/build**: Java 21, Maven — baseline LTS moderna. O Maven Wrapper elimina a necessidade de instalação prévia.
- **Framework**: Spring Boot 4.1.0 (Spring Framework 7), versão estável mais recente disponível.
- **Persistência**: H2 em arquivo por padrão, Postgres via profile. H2-arquivo garante execução sem dependência externa e sem perda de dados. Postgres cobre o cenário de produção.
- **Cliente HTTP externo**: `RestClient`, não `WebClient`. A aplicação é Spring MVC síncrona (não reativa) — `WebClient` exigiria toda a stack WebFlux/Reactor Netty sem necessidade real.
- **Concorrência de voto**: constraint única no banco (`sessao_votacao_id + associado_id`), garantia real de integridade que não depende só de checagem em Java.
- **Tempo/sessão**: `Instant` (UTC) mais `Clock` injetável. Evita ambiguidade de fuso e permite testar expiração de sessão com `Clock.fixed`, sem `Thread.sleep`.
- **Erros**: `@RestControllerAdvice` central, corpo de erro consistente (`titulo`/`mensagem`), nenhum stacktrace exposto ao cliente.
- **Versionamento de API**: URI (`/api/v1/...`), detalhado no [Bônus 3](#bônus-3--versionamento-da-api).

## Arquitetura

Arquitetura em camadas (`controller` → `service` → `repository`), deliberadamente sem hexagonal/ports-and-adapters. Para um domínio de 3 entidades e cerca de 15 endpoints, a cerimônia de portas e adapters adicionaria abstração sem ganho real, contrariando o pedido do próprio enunciado de evitar over engineering. A única fronteira isolada atrás de interface é a integração externa (`CpfEligibilidadeClient`), por ser o único ponto genuinamente instável e que precisa ser mockável em teste sem depender do serviço real.

```
controller/   REST, tradução HTTP ↔ domínio, validação de entrada
service/      regras de negócio
repository/   Spring Data JPA
domain/       entidades JPA (Pauta, SessaoVotacao, Voto)
dto/          records — contrato de tela do Anexo 1 e requests/responses
client/       integração com serviço externo de CPF
exception/    exceptions de domínio + handler global
config/       beans de infraestrutura (Clock, RestClient, OpenAPI)
util/         utilitários transversais (mascaramento de dado sensível em log)
```

## Contrato de tela (Anexo 1)

O foco do projeto é a comunicação backend↔mobile via mensagens que descrevem a tela a ser renderizada (padrão *server-driven UI*), não um REST de recursos convencional. Toda resposta consumida pelo app é um dos dois tipos definidos no anexo:

- **FORMULARIO** — título, campos de entrada, um ou dois botões (`botaoOk`/`botaoCancelar`), cada um disparando `POST` para uma URL com um `body`.
- **SELECAO** — título, lista de itens clicáveis, cada um disparando `POST` para uma URL com um `body`.

O anexo define a estrutura do envelope (nomes de campo, tipos de tela), e isso foi seguido literalmente. Os nomes de campo de negócio (`titulo`, `duracaoMinutos`, `associadoId` etc.) e as URLs ficam a critério da implementação, conforme nota explícita do próprio anexo: "o formato da URL é meramente ilustrativo e não define qualquer padrão de formato".

Erros (404/409/422/403/503) não seguem o formato de tela — respondem com HTTP status semântico mais corpo simples `{"titulo", "mensagem"}`, decisão documentada por não haver cobertura desse cenário no anexo.

## Fluxo de endpoints

```
GET  /api/v1/pautas                               lista pautas (SELECAO, com status resumido)
POST /api/v1/pautas/lista                         idem, via POST (usado pelos botões Cancelar)
POST /api/v1/pautas/novo                          formulário de nova pauta
POST /api/v1/pautas                               cria pauta
POST /api/v1/pautas/{id}/menu                     menu contextual da pauta

POST /api/v1/pautas/{id}/sessoes/novo             formulário de abertura de sessão
POST /api/v1/pautas/{id}/sessoes                  abre sessão (duracaoMinutos opcional, default 1 min)

POST /api/v1/pautas/{id}/votos/novo               formulário: informar id do associado
POST /api/v1/pautas/{id}/votos/opcoes             valida elegibilidade (CPF) → tela Sim/Não
POST /api/v1/pautas/{id}/votos/{associadoId}      registra o voto

POST /api/v1/pautas/{id}/resultado                contabiliza e retorna o resultado (sessão precisa estar encerrada)
```

O menu de cada pauta é contextual ao estado da sessão: mostra "Abrir sessão" (sem sessão), "Votar" (sessão aberta) ou "Ver resultado" (sessão encerrada).

## Persistência

- **Pauta** 1—1 **SessaoVotacao** (constraint única em `pauta_id`).
- **SessaoVotacao** 1—N **Voto**, com constraint única composta `(sessao_votacao_id, associado_id)` — garante "um voto por associado por pauta" diretamente no banco, mesmo que duas requisições do mesmo associado cheguem ao mesmo tempo.
- Nenhuma flag `aberta` persistida: o estado da sessão é sempre calculado comparando `Instant.now(clock)` com `dataFechamento`, o que elimina o risco de estado divergente do banco.
- Sem campos genéricos `createdAt`/`updatedAt`: os timestamps de domínio (`dataCriacao`, `dataAbertura`, `dataVoto`) já cobrem a auditoria de criação com mais significado. Como o escopo não inclui edição de dados, um campo `updatedAt` nunca seria atualizado — por isso foi deixado de fora.

## Tratamento de erros

- **404** — pauta/sessão não encontrada ou CPF inválido (serviço externo).
- **409** — sessão já aberta, voto duplicado, ou conflito de concorrência detectado pela constraint do banco (duas requisições simultâneas disputando o mesmo recurso).
- **422** — sessão fechada ao votar, ou sessão ainda aberta ao consultar resultado.
- **403** — associado não apto a votar.
- **503** — serviço de CPF indisponível.
- **400** — dados inválidos (`@Valid`) ou corpo malformado.
- **500** — qualquer erro não mapeado (mensagem genérica, sem stacktrace).

## Bônus 1 — Integração com CPF

Implementada via `RestClient`, com URL e timeout configuráveis por variável de ambiente (`CPF_SERVICE_URL`, `CPF_SERVICE_TIMEOUT_SECONDS`), conforme pedido no enunciado, para facilitar testes em diferentes ambientes.

> **Nota importante:** no momento do desenvolvimento, o serviço `https://user-info.herokuapp.com` citado no enunciado estava indisponível, retornando 404 para qualquer requisição. Para validar a integração sem depender desse serviço externo, a implementação foi testada de duas formas: de maneira automatizada, com `MockRestServiceServer` simulando as respostas exatas descritas no enunciado (CPF apto/inapto/inválido); e manualmente, contra um mock HTTP local reproduzindo o mesmo contrato. O client também trata timeout e erro de rede de forma resiliente (→ 503, sem derrubar a aplicação), cenário não coberto explicitamente no enunciado mas relevante para uma dependência externa em produção.

A checagem de CPF é executada pelo `VotoService`, não pelo controller, e ocorre em dois momentos: na etapa `/votos/opcoes` (falha cedo, antes de mostrar a tela de Sim/Não a um associado inapto) e novamente dentro de `votar()`, imediatamente antes de persistir o voto. A segunda checagem garante que a regra não possa ser contornada por uma chamada direta a `POST /votos/{associadoId}` sem passar pela etapa anterior — o `associadoId` chega por `@PathVariable`, então nada impede uma requisição de pular a tela intermediária. Não é tratada como uma camada de autenticação global, já que o enunciado abstrai explicitamente segurança/autenticação do escopo; é uma regra de negócio (associado elegível), aplicada onde o dado é efetivamente gravado.

Vale notar que o serviço externo retorna resultado aleatório a cada chamada (conforme o próprio enunciado descreve). Checar a elegibilidade duas vezes no mesmo fluxo significa que, em teoria, um associado aprovado na primeira checagem pode ser reprovado na segunda. Esse comportamento é inerente à natureza aleatória do serviço externo, não introduzido pela checagem dupla — e é o trade-off correto: validar apenas uma vez e confiar no resultado até a gravação abriria uma janela de tempo (entre a confirmação e a escrita) em que a regra poderia ser violada.

## Bônus 2 — Performance

- Contagem de votos feita via query agregada (`GROUP BY` no banco), nunca carregando os votos em memória para contar em Java, necessário para o cenário de "centenas de milhares de votos" citado no enunciado.
- Constraint única no banco evita a necessidade de lock aplicacional para checagem de voto duplicado.
- `open-in-view: false` evita manter conexão de banco aberta durante toda a renderização da resposta HTTP (anti-padrão Open Session In View), relevante sob carga concorrente.

### Teste de carga (k6)

Script em `loadtest/votar.js` simula votos concorrentes de associados distintos numa única sessão de votação aberta, com carga escalando de 0 a 50 usuários virtuais simultâneos.

**Como executar:**

```bash
./mvnw spring-boot:run          # em um terminal
./loadtest/preparar-cenario.sh  # cria a pauta e abre a sessão de votação
k6 run -e PAUTA_ID=<id-retornado> loadtest/votar.js
```

**Resultado de uma execução local** (35s, banco H2, máquina de desenvolvimento — não representa um ambiente de produção dimensionado, mas evidencia o comportamento sob concorrência):

- **79.634 votos processados**, 0 falhas (`http_req_failed: 0.00%`)
- Latência: p95 = 46,3 ms, p99 = 81,27 ms
- Throughput: ~2.275 requisições/segundo
- Nenhum erro ou exceção registrada no log da aplicação durante a execução

O resultado confirma que a constraint única do banco sustenta a regra de "um voto por associado" mesmo sob alta concorrência, sem necessidade de lock aplicacional, e que a query agregada de contabilização não é impactada pelo volume de escritas simultâneas. Para um cenário de produção real, o próximo passo seria repetir o teste contra o profile Postgres (não H2) e com carga escalada em ambiente dedicado, fora de uma máquina de desenvolvimento compartilhada com outros processos.

## Bônus 3 — Versionamento da API

Estratégia adotada: versionamento por URI (`/api/v1/...`).

Justificativa: é explícito, já que a versão fica visível na própria URL sem necessidade de inspecionar headers. É simples de testar em qualquer cliente HTTP e facilmente compatível com o padrão de roteamento de qualquer gateway ou proxy reverso na frente da API. O trade-off — duplicar controllers em versões futuras (`/api/v2/...`) quando houver mudança incompatível — é aceitável para o escopo e maturidade deste projeto. Alternativas como versionamento por header (`Accept-Version`) trazem mais flexibilidade, mas exigem mais disciplina de documentação e são menos óbvias para quem consome a API pela primeira vez.

## Testes e qualidade

- **61 testes automatizados**, cobertura de **97% de instruções / 90% de branches** (JaCoCo — relatório gerado em `target/site/jacoco/index.html` após `./mvnw test`).
- **Unitários** (JUnit 5 + Mockito): regras de negócio dos services, isoladas de banco e HTTP.
- **Fatia web** (`@WebMvcTest` + `MockMvc`): roteamento, serialização, validação e tradução de exceções pelo `@RestControllerAdvice`.
- **Integração com banco real** (`@DataJpaTest`): confirma que as constraints únicas (voto duplicado, sessão duplicada) são impostas pelo banco, não apenas pela aplicação.
- **Integração de cliente HTTP** (`MockRestServiceServer`): valida o `RestClient` do serviço de CPF contra os 3 cenários do enunciado, mais o cenário de indisponibilidade.

```bash
./mvnw test
```

## Docker

```bash
docker build -t voto-cooperativa .
docker run -p 8080:8080 voto-cooperativa
```

Build multi-stage (a imagem final não carrega JDK completo nem código-fonte) e execução como usuário não-root. Validado localmente com `docker build` e `docker run` antes da entrega.

## Assunções e decisões documentadas

- **Empate na votação**: o enunciado não define regra de desempate. A API reporta explicitamente "Empate" em vez de forçar aprovação ou reprovação arbitrária.
- **Botão Cancelar**: tratado como uma ação de navegação igual ao botão principal (também dispara `POST`), por consistência — o anexo não detalha esse comportamento além de citá-lo como opcional.
- **Formato do `associadoId`**: validado como CPF (11 dígitos numéricos) desde a coleta do dado, já que o Bônus 1 depende dessa identificação ser um CPF válido. Isso permite falhar rápido (400) antes mesmo de consultar o serviço externo.
- **Segurança/autenticação**: abstraída, conforme instrução explícita do enunciado ("qualquer chamada pode ser considerada autorizada").
- **Console web do H2**: habilitado em `/h2-console` apenas no profile padrão (desenvolvimento local com H2), sem autenticação — coerente com a abstração de segurança do enunciado, mas por prudência é explicitamente desabilitado no profile `postgres`, que representa um cenário mais próximo de produção.
