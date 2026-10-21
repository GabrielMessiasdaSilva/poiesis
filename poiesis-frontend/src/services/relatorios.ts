import api from './api';

export type RelatorioVendas = { totalPedidos: number; faturamentoTotal: number; ticketMedio: number };
export type ResumoProducao = { emPendente: number; emCorte: number; emCostura: number; emAcabamento: number; concluidos: number };
export const relatoriosService = {
    async consultarVendas(dataInicio: string, dataFim: string): Promise<RelatorioVendas> {
        return (await api.get<RelatorioVendas>('/v1/relatorios/vendas', { params: { dataInicio, dataFim } })).data;
    },
    async consultarProducao(): Promise<ResumoProducao> {
        return (await api.get<ResumoProducao>('/v1/relatorios/producao')).data;
    },
};

export function periodoValido(inicio: string, fim: string): boolean {
    const valida = (valor: string) => {
        if (!/^\d{4}-\d{2}-\d{2}$/.test(valor)) return false;
        const date = new Date(`${valor}T12:00:00Z`);
        return Number.isFinite(date.getTime()) && date.toISOString().slice(0, 10) === valor;
    };
    return valida(inicio) && valida(fim) && inicio <= fim;
}
