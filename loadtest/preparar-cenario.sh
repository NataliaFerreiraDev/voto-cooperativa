#!/usr/bin/env bash
# Prepara o cenário para o teste de carga (Bônus 2): cria uma pauta e abre
# uma sessão de votação com duração suficiente para o teste k6 rodar.
#
# Uso: ./loadtest/preparar-cenario.sh [BASE_URL]

set -e

BASE_URL="${1:-http://localhost:8080}"

echo "Criando pauta de teste de carga..."
RESPOSTA=$(curl -s -X POST "$BASE_URL/api/v1/pautas" \
    -H "Content-Type: application/json" \
    -d '{"titulo":"Pauta - teste de carga","descricao":"Gerada por loadtest/preparar-cenario.sh"}')

echo "$RESPOSTA"

PAUTA_ID=$(echo "$RESPOSTA" | grep -oP '(?<=/pautas/)\d+(?=/menu)' | tail -1)

if [ -z "$PAUTA_ID" ]; then
    echo "Não foi possível determinar o id da pauta criada. Verifique a resposta acima."
    exit 1
fi

echo "Pauta criada: id=$PAUTA_ID"
echo "Abrindo sessão de votação (10 minutos)..."

curl -s -X POST "$BASE_URL/api/v1/pautas/$PAUTA_ID/sessoes" \
    -H "Content-Type: application/json" \
    -d '{"duracaoMinutos": 10}'

echo ""
echo ""
echo "Cenário pronto. Execute o teste de carga com:"
echo "  k6 run -e PAUTA_ID=$PAUTA_ID loadtest/votar.js"
