import api from './api';

export const statusProducao = ['PENDENTE', 'APROVADO', 'EM_CORTE', 'EM_COSTURA', 'EM_PERSONALIZACAO', 'FINALIZADO', 'ENTREGUE', 'CANCELADO'] as const;
export type StatusProducao = typeof statusProducao[number];
export type OrdemProducao = {
    id: number; pedidoId: number; clienteEmail: string; status: StatusProducao;
    dataInicio: string; dataAtualizacao: string;
    historico: { status: StatusProducao; dataHora: string }[];
};
export const producaoService = {
    async listar(): Promise<OrdemProducao[]> {
        return (await api.get<OrdemProducao[]>('/v1/producao')).data;
    },
    async consultarPedido(pedidoId: number): Promise<OrdemProducao> {
        return (await api.get<OrdemProducao>(`/v1/producao/pedidos/${pedidoId}`)).data;
    },
    async atualizarStatus(id: number, status: StatusProducao): Promise<OrdemProducao> {
        return (await api.patch<OrdemProducao>(`/v1/producao/${id}/status`, { status })).data;
    },
};
