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

process_matches_service() {
    local pid="$1" service="$2" index jar cwd
    local -a args=()
    [[ "$pid" =~ ^[0-9]+$ && -r "/proc/$pid/cmdline" ]] || return 1
    mapfile -d '' -t args < "/proc/$pid/cmdline" 2>/dev/null || return 1
    [[ "${args[0]:-}" != "" && "${args[0]##*/}" == "java" ]] || return 1
    for ((index = 1; index < ${#args[@]} - 1; index++)); do
        [[ "${args[index]}" == "-jar" ]] || continue
        jar="${args[index + 1]}"
        if [[ "$jar" != /* ]]; then
            cwd="$(readlink "/proc/$pid/cwd")" || return 1
            jar="$cwd/$jar"
        fi
        [[ "$(readlink -m "$jar")" == "$(service_jar "$service")" ]]
        return
    done
    return 1
}

service_pids() {
    local service="$1" pid executable
    # Recupera também processos cujos arquivos .pid foram perdidos.
    while read -r pid executable; do
        [[ "$executable" == "java" ]] || continue
        if process_matches_service "$pid" "$service"; then
            printf '%s\n' "$pid"
        fi
    done < <(ps -eo pid=,comm=)
}

stop_services() {
    local service pid index pending failed=0
    local -a stopped_pids=() stopped_services=()
    for service in "${SERVICES[@]}"; do
        while read -r pid; do
            [[ -n "$pid" ]] || continue
            if process_matches_service "$pid" "$service"; then
                if kill -TERM "$pid" 2>/dev/null; then
                    printf 'Encerrando %s (PID %s)\n' "$service" "$pid"
                fi
                stopped_pids+=("$pid")
                stopped_services+=("$service")
            fi
        done < <(service_pids "$service")
    done

    for _ in {1..30}; do
        pending=0
        for index in "${!stopped_pids[@]}"; do
            if process_matches_service "${stopped_pids[index]}" "${stopped_services[index]}"; then
                pending=1
            fi
        done
        (( pending )) || break
        sleep 1
    done

    for index in "${!stopped_pids[@]}"; do
        pid="${stopped_pids[index]}"
        service="${stopped_services[index]}"
        if process_matches_service "$pid" "$service"; then
            printf '%s não encerrou em 30 segundos; forçando parada (PID %s).\n' "$service" "$pid"
            kill -KILL "$pid" 2>/dev/null || true
        fi
    done

    for _ in {1..5}; do
        pending=0
        for index in "${!stopped_pids[@]}"; do
            if process_matches_service "${stopped_pids[index]}" "${stopped_services[index]}"; then
                pending=1
            fi
        done
        (( pending )) || break
        sleep 1
    done

    for service in "${SERVICES[@]}"; do
        pending="$(service_pids "$service")"
        if [[ -n "$pending" ]]; then
            printf 'Não foi possível encerrar %s (PIDs: %s).\n' "$service" "$pending" >&2
            failed=1
        else
            rm -f "$RUN_DIR/$service.pid"
        fi
        if port_is_open "$(service_port "$service")"; then
            printf 'A porta %s (%s) continua ocupada.\n' "$(service_port "$service")" "$service" >&2
            failed=1
        fi
    done
    (( failed == 0 )) || return 1
    printf 'Serviços encerrados; portas 8080 a 8086 livres.\n'
}

port_is_open() {
    local port="$1"
    ( : > "/dev/tcp/127.0.0.1/$port" ) 2>/dev/null
}

start_service() {
    local service="$1" port="$2" jar="$3"
    local pid_file="$RUN_DIR/$service.pid"
    if port_is_open "$port"; then
        printf 'A porta %s (%s) já está ocupada. Encerre o processo existente e tente novamente.\n' "$port" "$service" >&2
        stop_services
        exit 1
    fi

    POIESIS_AUTH_DB="${POIESIS_AUTH_DB:-$RUN_DIR/auth}" nohup java -jar "$jar" > "$LOG_DIR/$service.log" 2>&1 < /dev/null 9>&- &
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

if [[ $# -gt 1 || ( $# -eq 1 && "$1" != "parar" && "$1" != "stop" ) ]]; then
    printf 'Uso: %s [parar]\n' "$0" >&2
    exit 2
fi

command -v flock >/dev/null || { echo 'flock não encontrado no PATH (pacote util-linux).' >&2; exit 1; }
mkdir -p "$LOG_DIR"
exec 9> "$RUN_DIR/control.lock"
flock -n 9 || { echo 'Outra inicialização/parada está em andamento.' >&2; exit 1; }

if [[ "${1:-}" == "parar" || "${1:-}" == "stop" ]]; then
    stop_services
    exit 0
fi

command -v java >/dev/null || { echo 'Java não encontrado no PATH.' >&2; exit 1; }
command -v mvn >/dev/null || { echo 'Maven não encontrado no PATH.' >&2; exit 1; }

java_version="$(java -version 2>&1 | head -n 1)"
if [[ "$java_version" != *'21.'* ]]; then
    printf 'Este projeto requer Java 21. Encontrado: %s\n' "$java_version" >&2
    exit 1
fi

for service in "${SERVICES[@]}"; do
    if port_is_open "$(service_port "$service")" || [[ -n "$(service_pids "$service")" ]]; then
        printf '%s já está ativo ou sua porta está ocupada. Execute %s parar antes de iniciar.\n' "$service" "$0" >&2
        exit 1
    fi
done

for service in "${SERVICES[@]}"; do
    printf '\n=== Compilando e instalando %s ===\n' "$service"
    mvn -f "$ROOT/$service/pom.xml" -DskipTests clean install
done

trap 'trap - ERR INT TERM; stop_services || true; exit 1' ERR INT TERM
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
