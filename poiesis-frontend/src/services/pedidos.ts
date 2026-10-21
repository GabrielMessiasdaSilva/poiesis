import api from './api';

export type CustomizacaoEscolhida = { id: number; tipo: string; nome: string; precoAdicional: number };
export type ItemPedido = {
    produtoId: number; nomeProduto: string; quantidade: number;
    precoUnitario: number; subtotal: number; customizacoes: CustomizacaoEscolhida[];
    textoPersonalizado?: string | null; referenciaImagem?: string | null;
};
export type Pedido = {
    id: number; clienteEmail: string; itens: ItemPedido[]; valorTotal: number;
    status: 'CRIADO' | 'EM_PROCESSAMENTO' | 'FINALIZADO' | 'CANCELADO'; dataCriacao: string;
};
export type NovoPedido = { itens: { produtoId: number; quantidade: number; customizacaoIds?: number[]; textoPersonalizado?: string; referenciaImagem?: string }[] };

export const pedidosService = {
    async listar(): Promise<Pedido[]> {
        return (await api.get<Pedido[]>('/v1/pedidos')).data;
    },
    async criar(dados: NovoPedido): Promise<Pedido> {
        // O backend identifica o cliente e calcula os valores; enviamos somente a escolha.
        return (await api.post<Pedido>('/v1/pedidos', dados)).data;
    },
};
