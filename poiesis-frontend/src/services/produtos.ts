import api from './api';

export const categorias = ['CAMISA', 'CAMISETA', 'MOLETOM', 'CALCA', 'ACESSORIO'] as const;
export type Categoria = typeof categorias[number];
export type DadosProduto = { nome: string; descricao: string; precoBase: number; categoria: Categoria; imagemUrl?: string };
export type Produto = Omit<DadosProduto, 'descricao'> & { id: number; descricao: string | null; ativo: boolean; imagemUrl?: string | null };
export type DadosCustomizacao = { produtoId: number; tipo: string; nome: string; precoAdicional: number };
export type OpcaoCustomizacao = DadosCustomizacao & { id: number; ativo: boolean };
export type OpcoesProduto = { produto: Produto; opcoes: OpcaoCustomizacao[] };

// Catálogo e customizações pertencem a Produto; suas APIs públicas passam pelo gateway.
export const produtosService = {
    async listar(): Promise<Produto[]> {
        return (await api.get<Produto[]>('/v1/produtos')).data;
    },
    async cadastrar(dados: DadosProduto): Promise<Produto> {
        return (await api.post<Produto>('/v1/produtos', dados)).data;
    },
    async atualizar(id: number, dados: DadosProduto): Promise<Produto> {
        return (await api.put<Produto>(`/v1/produtos/${id}`, dados)).data;
    },
    async inativar(id: number): Promise<void> {
        await api.delete(`/v1/produtos/${id}`);
    },
};

export const customizacoesService = {
    async listarPorProduto(produtoId: number): Promise<OpcaoCustomizacao[]> {
        const { data } = await api.get<OpcaoCustomizacao[]>(`/v1/customizacoes/produto/${produtoId}`);
        return data.filter(opcao => opcao.ativo && opcao.produtoId === produtoId);
    },
    async listarComProdutos(): Promise<OpcoesProduto[]> {
        const produtos = await produtosService.listar();
        return Promise.all(produtos.map(async produto => ({
            produto, opcoes: await customizacoesService.listarPorProduto(produto.id),
        })));
    },
    async cadastrar(dados: DadosCustomizacao): Promise<OpcaoCustomizacao> {
        return (await api.post<OpcaoCustomizacao>('/v1/customizacoes', dados)).data;
    },
    async atualizar(id: number, dados: DadosCustomizacao): Promise<OpcaoCustomizacao> {
        return (await api.put<OpcaoCustomizacao>(`/v1/customizacoes/${id}`, dados)).data;
    },
    async inativar(id: number): Promise<void> {
        await api.delete(`/v1/customizacoes/${id}`);
    },
};
