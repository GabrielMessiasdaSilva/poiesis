#!/usr/bin/env bash
set -Eeuo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RUN_DIR="$ROOT/.run-local"
LOG_DIR="$RUN_DIR/logs"

SERVICES=(login catalogo pedido producao relatorio customizacao gateway)

service_jar() {
    local service="$1"
    if [[ "$service" == "gateway" ]]; then
        printf '%s/gateway/target/gateway-0.0.1-SNAPSHOT.jar' "$ROOT"
    else
        printf '%s/%s/springframework/target/springframework-0.0.1-SNAPSHOT.jar' "$ROOT" "$service"
    fi
}

service_port() {
    case "$1" in
        login) printf 8081 ;;
        catalogo) printf 8082 ;;
        pedido) printf 8083 ;;
        relatorio) printf 8084 ;;
        producao) printf 8085 ;;
        customizacao) printf 8086 ;;
        gateway) printf 8080 ;;
    esac
}

stop_services() {
    local service pid jar command
    for service in "${SERVICES[@]}"; do
        local pid_file="$RUN_DIR/$service.pid"
        [[ -f "$pid_file" ]] || continue
        pid="$(cat "$pid_file")"
        jar="$(service_jar "$service")"
        if kill -0 "$pid" 2>/dev/null; then
            command="$(ps -p "$pid" -o args= 2>/dev/null || true)"
            if [[ "$command" == *"$jar"* ]]; then
                kill "$pid"
                printf 'Encerrando %s (PID %s)\n' "$service" "$pid"
            else
                printf 'PID salvo para %s não corresponde mais ao serviço; não foi encerrado.\n' "$service"
            fi
        fi
        rm -f "$pid_file"
    done
}

port_is_open() {
    local port="$1"
    ( : > "/dev/tcp/127.0.0.1/$port" ) 2>/dev/null
}

start_service() {
    local service="$1" port="$2" jar="$3" pid_file="$RUN_DIR/$service.pid"
    if port_is_open "$port"; then
        printf 'A porta %s (%s) já está ocupada. Encerre o processo existente e tente novamente.\n' "$port" "$service" >&2
        stop_services
        exit 1
    fi

    nohup java -jar "$jar" > "$LOG_DIR/$service.log" 2>&1 < /dev/null &
    local pid=$!
    printf '%s\n' "$pid" > "$pid_file"

    for _ in {1..120}; do
        if ! kill -0 "$pid" 2>/dev/null; then
            printf '%s encerrou durante a inicialização. Últimas linhas do log:\n' "$service" >&2
            tail -n 40 "$LOG_DIR/$service.log" >&2 || true
            stop_services
            exit 1
        fi
        if port_is_open "$port"; then
            printf '%s iniciado na porta %s (PID %s)\n' "$service" "$port" "$pid"
            return
        fi
        sleep 1
    done

    printf 'Tempo esgotado aguardando a porta %s de %s. Últimas linhas do log:\n' "$port" "$service" >&2
    tail -n 40 "$LOG_DIR/$service.log" >&2 || true
    stop_services
    exit 1
}

if [[ "${1:-}" == "parar" || "${1:-}" == "stop" ]]; then
    stop_services
    exit 0
fi

if [[ $# -gt 0 ]]; then
    printf 'Uso: %s [parar]\n' "$0" >&2
    exit 2
fi

command -v java >/dev/null || { echo 'Java não encontrado no PATH.' >&2; exit 1; }
command -v mvn >/dev/null || { echo 'Maven não encontrado no PATH.' >&2; exit 1; }

java_version="$(java -version 2>&1 | head -n 1)"
if [[ "$java_version" != *'21.'* ]]; then
    printf 'Este projeto requer Java 21. Encontrado: %s\n' "$java_version" >&2
    exit 1
fi

mkdir -p "$LOG_DIR"

for service in "${SERVICES[@]}"; do
    printf '\n=== Compilando e instalando %s ===\n' "$service"
    mvn -f "$ROOT/$service/pom.xml" -DskipTests install
done

printf '\n=== Iniciando os microsserviços ===\n'
start_service catalogo 8082 "$(service_jar catalogo)"
start_service login 8081 "$(service_jar login)"
start_service pedido 8083 "$(service_jar pedido)"
start_service producao 8085 "$(service_jar producao)"
start_service relatorio 8084 "$(service_jar relatorio)"
start_service customizacao 8086 "$(service_jar customizacao)"
start_service gateway 8080 "$(service_jar gateway)"

printf '\nProjeto iniciado. Gateway: http://localhost:8080\n'
printf 'Logs: %s\n' "$LOG_DIR"
printf 'Para encerrar: %s parar\n' "$0"
