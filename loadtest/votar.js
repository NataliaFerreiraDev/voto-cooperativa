import http from 'k6/http';
import { check } from 'k6';
import { Counter } from 'k6/metrics';

// Teste de carga do endpoint de votação (Bônus 2 - Performance).
//
// Simula votos concorrentes de associados distintos numa única sessão de
// votação aberta, medindo latência e taxa de erro sob carga.
//
// Pré-requisito: a aplicação precisa estar rodando (./mvnw spring-boot:run)
// e já deve existir uma pauta com sessão de votação ABERTA. O id da pauta
// e a duração mínima necessária da sessão são configuráveis via variáveis
// de ambiente (ver README, seção "Bônus 2 — Performance").
//
// Execução:
//   k6 run loadtest/votar.js
//   k6 run -e PAUTA_ID=2 -e BASE_URL=http://localhost:8080 loadtest/votar.js

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const PAUTA_ID = __ENV.PAUTA_ID || '1';

const votosComSucesso = new Counter('votos_com_sucesso');
const votosComErro = new Counter('votos_com_erro');

export const options = {
    scenarios: {
        votacao_concorrente: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '10s', target: 50 },
                { duration: '20s', target: 50 },
                { duration: '5s', target: 0 },
            ],
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<500', 'p(99)<1000'],
        http_req_failed: ['rate<0.01'],
    },
};

export default function () {
    // cada VU + iteração gera um associadoId numérico único de 11 dígitos,
    // evitando colisão de "voto duplicado" (que mascararia a métrica real de performance)
    const associadoId = String(1000000000 + (__VU * 100000 + __ITER)).padStart(11, '0').slice(-11);

    const payload = JSON.stringify({ opcao: Math.random() > 0.5 ? 'SIM' : 'NAO' });
    const params = { headers: { 'Content-Type': 'application/json' } };

    const res = http.post(
        `${BASE_URL}/api/v1/pautas/${PAUTA_ID}/votos/${associadoId}`,
        payload,
        params
    );

    const sucesso = check(res, {
        'status é 200': (r) => r.status === 200,
    });

    if (sucesso) {
        votosComSucesso.add(1);
    } else {
        votosComErro.add(1);
    }
}
