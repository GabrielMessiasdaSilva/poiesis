import { isAxiosError } from 'axios';

export function mensagemApi(error: unknown, fallback = 'Não foi possível concluir a solicitação. Tente novamente.'): string {
    if (isAxiosError<{ message?: unknown; erro?: unknown }>(error)) {
        if (error.response?.status === 401) return 'Sua sessão expirou. Entre novamente.';
        if (error.response?.status === 403) return 'Sua conta não tem permissão para esta operação.';
        const data = error.response?.data;
        const mensagem = data?.message ?? data?.erro;
        if (typeof mensagem === 'string' && mensagem.trim()) return mensagem;
        if (error.response?.status === 409) return 'Os dados foram alterados ou já existem. Atualize a consulta antes de continuar.';
        if ((error.response?.status ?? 0) >= 500) return 'O serviço está temporariamente indisponível. Consulte seus dados antes de tentar novamente.';
        if (!error.response) return 'Não foi possível acessar a API. Verifique sua conexão e tente novamente.';
    }
    return error instanceof Error && !isAxiosError(error) ? error.message : fallback;
}
