import { useCallback, useRef, useState } from 'react';
import { ActivityIndicator, Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { useFocusEffect } from 'expo-router';
import { produtosService, customizacoesService, categorias, type Categoria, type Produto, type OpcaoCustomizacao } from '../services/produtos';
import { producaoService, type OrdemProducao } from '../services/producao';
import { mensagemApi } from '../services/erros';
import { Button, Field, Notice } from '../ui/components';
import { colors, serif } from '../ui/theme';

const vazio = { nome: '', descricao: '', preco: '', categoria: 'CAMISETA' as Categoria, tipo: '', imagemUrl: '' };
const dinheiro = (valor: number) => `R$ ${Number(valor).toFixed(2).replace('.', ',')}`;
const proximaEtapa = (status: OrdemProducao['status']) => ({
    PENDENTE: 'APROVADO', APROVADO: 'EM_CORTE', EM_CORTE: 'EM_COSTURA',
    EM_COSTURA: 'EM_PERSONALIZACAO', EM_PERSONALIZACAO: 'FINALIZADO', FINALIZADO: 'ENTREGUE',
} as Partial<Record<OrdemProducao['status'], OrdemProducao['status']>>)[status];

function Botao({ titulo, onPress, disabled = false, perigo = false }: { titulo: string; onPress: () => void; disabled?: boolean; perigo?: boolean }) {
    return <Button title={titulo} onPress={onPress} disabled={disabled} danger={perigo} secondary={titulo === 'Cancelar' || titulo === 'Atualizar' || titulo === 'Editar'} />;
}

export default function AdminGestao({ secao }: { secao: 'Produtos' | 'Customizações' | 'Produção' }) {
    const [produtos, setProdutos] = useState<Produto[]>([]);
    const [opcoes, setOpcoes] = useState<OpcaoCustomizacao[]>([]);
    const [ordens, setOrdens] = useState<OrdemProducao[]>([]);
    const [produtoId, setProdutoId] = useState<number | null>(null);
    const [form, setForm] = useState(vazio);
    const [editando, setEditando] = useState<number | null>(null);
    const [aberto, setAberto] = useState(false);
    const [excluir, setExcluir] = useState<number | null>(null);
    const [busy, setBusy] = useState(false);
    const [carregando, setCarregando] = useState(false);
    const [erro, setErro] = useState('');
    const [sucesso, setSucesso] = useState('');
    const revisao = useRef(0);
    const trava = useRef(false);

    const carregar = useCallback(async () => {
        const atual = ++revisao.current;
        setCarregando(true); setErro('');
        try {
            if (secao === 'Produção') {
                const data = await producaoService.listar();
                if (atual === revisao.current) setOrdens(data);
            } else {
                const data = await produtosService.listar();
                if (atual !== revisao.current) return;
                setProdutos(data);
                if (secao === 'Customizações' && produtoId && !data.some(produto => produto.id === produtoId)) {
                    setProdutoId(null); setOpcoes([]); setAberto(false); setEditando(null);
                    return;
                }
                if (secao === 'Customizações' && produtoId) {
                    const opcoesProduto = await customizacoesService.listarPorProduto(produtoId);
                    if (atual === revisao.current) setOpcoes(opcoesProduto);
                } else setOpcoes([]);
            }
        } catch (error) { if (atual === revisao.current) setErro(mensagemApi(error)); }
        finally { if (atual === revisao.current) setCarregando(false); }
    }, [secao, produtoId]);

    useFocusEffect(useCallback(() => {
        void carregar();
        return () => { revisao.current++; };
    }, [carregar]));

    function novo() { setForm(vazio); setEditando(null); setAberto(true); setExcluir(null); setErro(''); setSucesso(''); }
    async function executar(acao: () => Promise<void>, mensagem: string) {
        if (trava.current) return;
        trava.current = true; setBusy(true); setErro(''); setSucesso('');
        try { await acao(); setSucesso(mensagem); await carregar(); }
        catch (error) { setErro(mensagemApi(error)); }
        finally { trava.current = false; setBusy(false); }
    }
    async function salvar() {
        const textoPreco = form.preco.trim().replace(',', '.');
        const preco = Number(textoPreco);
        if (!form.nome.trim() || !/^\d+(\.\d{1,2})?$/.test(textoPreco) || !Number.isFinite(preco) || preco >= 100000000 || preco < 0 || (secao === 'Produtos' && preco === 0)) {
            setErro('Informe nome e preço válido com até duas casas decimais. O preço do produto deve ser maior que zero.'); return;
        }
        if (secao === 'Customizações' && (!produtoId || !form.tipo.trim())) { setErro('Selecione um produto e informe o tipo da customização.'); return; }
        await executar(async () => {
            if (secao === 'Produtos') {
                const dados = { nome: form.nome.trim(), descricao: form.descricao.trim(), precoBase: preco, categoria: form.categoria, imagemUrl: form.imagemUrl.trim() };
                if (editando !== null) await produtosService.atualizar(editando, dados);
                else await produtosService.cadastrar(dados);
            } else {
                if (produtoId === null) throw new Error('Selecione um produto.');
                const dados = { produtoId, nome: form.nome.trim(), tipo: form.tipo.trim(), precoAdicional: preco };
                if (editando !== null) await customizacoesService.atualizar(editando, dados);
                else await customizacoesService.cadastrar(dados);
            }
            setAberto(false); setEditando(null); setForm(vazio);
        }, 'Registro salvo com sucesso.');
    }
    const campo = (chave: keyof typeof vazio, label: string, maxLength?: number) => <Field label={label} value={form[chave]} editable={!busy} maxLength={maxLength}
        keyboardType={chave === 'preco' ? 'decimal-pad' : 'default'} multiline={chave === 'descricao'}
        onChangeText={valor => setForm(atual => ({ ...atual, [chave]: valor }))} />;

    return <View style={{ gap: 14 }}>
        <View style={styles.linha}><Text style={styles.titulo}>{secao}</Text><Botao titulo="Atualizar" disabled={busy || carregando} onPress={() => void carregar()} /></View>
        {erro ? <Notice error>{erro}</Notice> : null}
        {sucesso ? <Notice>{sucesso}</Notice> : null}
        {secao === 'Customizações' && <>
            <Text style={styles.label}>Selecione o produto</Text>
            <ScrollView horizontal><View style={styles.linha}>{produtos.map(p => <Pressable key={p.id} disabled={busy} accessibilityRole="button" accessibilityState={{ selected: produtoId === p.id }}
                onPress={() => { setProdutoId(p.id); setOpcoes([]); setAberto(false); setExcluir(null); setSucesso(''); }}
                style={[styles.chip, produtoId === p.id && styles.selecionado]}><Text style={{ color: colors.primary }}>{p.nome}</Text></Pressable>)}</View></ScrollView>
            {!produtoId && <Text>Escolha um produto para gerenciar suas opções.</Text>}
        </>}
        {secao !== 'Produção' && <Botao titulo={secao === 'Produtos' ? 'Cadastrar produto' : 'Cadastrar customização'} disabled={busy || carregando || (secao === 'Customizações' && !produtoId)} onPress={novo} />}
        {aberto && <View style={styles.card}>
            <Text style={styles.titulo}>{editando === null ? 'Novo cadastro' : `Editar #${editando}`}</Text>
            {campo('nome', 'Nome', 160)}
            {secao === 'Produtos' ? <>{campo('descricao', 'Descrição', 255)}{campo('imagemUrl', 'URL da foto da camiseta', 2048)}<Text style={{ color: colors.muted, fontSize: 12 }}>Use uma imagem própria, licenciada ou de banco com uso permitido. A URL deve iniciar com http:// ou https://.</Text><Text style={styles.label}>Categoria</Text><View style={styles.linha}>{categorias.map(c => <Pressable key={c} disabled={busy} accessibilityRole="button" accessibilityState={{ selected: form.categoria === c }} onPress={() => setForm(atual => ({ ...atual, categoria: c }))} style={[styles.chip, form.categoria === c && styles.selecionado]}><Text style={{ color: colors.primary }}>{c}</Text></Pressable>)}</View></> : campo('tipo', 'Tipo (ex.: COR, TAMANHO, ESTAMPA)', 80)}
            {campo('preco', secao === 'Produtos' ? 'Preço base (R$)' : 'Preço adicional (R$)')}
            <View style={styles.linha}><Botao titulo="Salvar" disabled={busy} onPress={() => void salvar()} /><Botao titulo="Cancelar" disabled={busy} onPress={() => { setAberto(false); setErro(''); }} /></View>
        </View>}
        {(busy || carregando) && <ActivityIndicator color={colors.primary} />}
        {!carregando && secao === 'Produtos' && !produtos.length && <Text>Nenhum produto ativo. Cadastre o primeiro produto.</Text>}
        {!carregando && secao === 'Customizações' && produtoId && !opcoes.length && <Text>Nenhuma customização ativa para este produto.</Text>}
        {(secao === 'Produtos' ? produtos : secao === 'Customizações' ? opcoes : []).map(item => <View key={item.id} style={styles.card}>
            <Text style={styles.titulo}>{item.nome}</Text>
            {'precoBase' in item ? <Text>{item.categoria} · {dinheiro(item.precoBase)}{'\n'}{item.descricao}</Text> : <Text>{item.tipo} · {dinheiro(item.precoAdicional)}</Text>}
            <View style={styles.linha}><Botao titulo="Editar" disabled={busy || carregando} onPress={() => {
                setEditando(item.id); setExcluir(null); setAberto(true); setErro(''); setSucesso('');
                setForm('precoBase' in item ? { ...vazio, nome: item.nome, descricao: item.descricao ?? '', categoria: item.categoria, preco: String(item.precoBase), imagemUrl: item.imagemUrl ?? '' } : { ...vazio, nome: item.nome, tipo: item.tipo, preco: String(item.precoAdicional) });
            }} /><Botao titulo="Excluir" perigo disabled={busy || carregando} onPress={() => setExcluir(item.id)} /></View>
            {excluir === item.id && <><Text>Excluir “{item.nome}”? O registro será inativado e deixará de aparecer nas opções disponíveis.</Text><View style={styles.linha}>
                <Botao titulo="Confirmar exclusão" perigo disabled={busy} onPress={() => void executar(async () => {
                    if (secao === 'Produtos') await produtosService.inativar(item.id);
                    else await customizacoesService.inativar(item.id);
                    setExcluir(null); if (editando === item.id) setAberto(false);
                }, 'Registro excluído das opções disponíveis.')} /><Botao titulo="Cancelar" disabled={busy} onPress={() => setExcluir(null)} />
            </View></>}
        </View>)}
        {secao === 'Produção' && <>
            {!carregando && !ordens.length && <Text>Nenhuma ordem de produção disponível.</Text>}
            {ordens.map(ordem => {
                const proxima = proximaEtapa(ordem.status);
                const encerrada = ['ENTREGUE', 'CANCELADO'].includes(ordem.status);
                return <View key={ordem.id} style={styles.ordemCard}>
                    <View style={styles.ordemCabecalho}>
                        <View style={{ gap: 4 }}><Text style={styles.titulo}>Pedido #{ordem.pedidoId}</Text><Text style={styles.cliente}>{ordem.clienteEmail}</Text></View>
                        <View style={styles.status}><Text style={styles.statusTexto}>{ordem.status.replaceAll('_', ' ')}</Text></View>
                    </View>
                    <View style={styles.etapas}>{['PENDENTE', 'APROVADO', 'EM_CORTE', 'EM_COSTURA', 'EM_PERSONALIZACAO', 'FINALIZADO', 'ENTREGUE'].map(etapa => <View key={etapa} style={[styles.pontoEtapa, etapa === ordem.status && styles.pontoEtapaAtual]}><Text style={[styles.pontoTexto, etapa === ordem.status && styles.pontoTextoAtual]}>{etapa.replace('EM_', '')}</Text></View>)}</View>
                    {!encerrada && <View style={{ gap: 8 }}><Text style={styles.label}>Próxima ação</Text><View style={styles.linha}>
                        {proxima && <Botao titulo={`Avançar para ${proxima.replaceAll('_', ' ')}`} disabled={busy || carregando} onPress={() => void executar(async () => { await producaoService.atualizarStatus(ordem.id, proxima); }, 'Etapa de produção atualizada.')}/>} 
                        <Botao titulo="Cancelar produção" perigo disabled={busy || carregando} onPress={() => void executar(async () => { await producaoService.atualizarStatus(ordem.id, 'CANCELADO'); }, 'Produção cancelada.')}/>
                    </View></View>}
                </View>;
            })}
        </>}
    </View>;
}

const styles = StyleSheet.create({
    titulo: { fontSize: 23, fontFamily: serif, color: colors.ink },
    label: { fontWeight: '600', color: colors.primary },
    linha: { flexDirection: 'row', flexWrap: 'wrap', gap: 8, alignItems: 'center' },
    card: { padding: 22, backgroundColor: colors.paper, borderRadius: 14, borderWidth: 1, borderColor: colors.line, gap: 12 },
    input: { borderWidth: 1, borderColor: colors.line, borderRadius: 10, padding: 12, color: colors.ink, backgroundColor: colors.paper },
    botao: { padding: 12, minHeight: 44, backgroundColor: colors.primary, borderRadius: 10, justifyContent: 'center' },
    botaoTexto: { color: colors.paper, fontWeight: '600' },
    perigo: { backgroundColor: colors.error },
    chip: { padding: 12, minHeight: 44, borderWidth: 1, borderColor: colors.line, borderRadius: 10 },
    selecionado: { backgroundColor: colors.soft, borderColor: colors.primary },
    erro: { color: colors.error },
    sucesso: { color: colors.primary },
    ordemCard: { padding: 22, backgroundColor: colors.paper, borderRadius: 16, borderWidth: 1, borderColor: colors.line, gap: 18 },
    ordemCabecalho: { flexDirection: 'row', alignItems: 'flex-start', justifyContent: 'space-between', gap: 12 },
    cliente: { color: colors.muted, fontSize: 13 },
    status: { backgroundColor: colors.soft, borderRadius: 20, paddingHorizontal: 10, paddingVertical: 7 },
    statusTexto: { color: colors.primary, fontSize: 11, fontWeight: '700', letterSpacing: .4 },
    etapas: { flexDirection: 'row', flexWrap: 'wrap', gap: 6 },
    pontoEtapa: { paddingHorizontal: 9, paddingVertical: 6, borderRadius: 14, backgroundColor: colors.background },
    pontoEtapaAtual: { backgroundColor: colors.primary },
    pontoTexto: { color: colors.muted, fontSize: 10, fontWeight: '600' },
    pontoTextoAtual: { color: colors.paper },
});
