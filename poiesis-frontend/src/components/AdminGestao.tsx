import { useCallback, useRef, useState } from 'react';
import { ActivityIndicator, Pressable, ScrollView, StyleSheet, Text, TextInput, View } from 'react-native';
import { useFocusEffect } from 'expo-router';
import { isAxiosError } from 'axios';
import api from '../services/api';

type Produto = { id: number; nome: string; descricao: string; precoBase: number; categoria: string };
type Opcao = { id: number; produtoId: number; nome: string; tipo: string; precoAdicional: number; ativo: boolean };
type Ordem = { id: number; pedidoId: number; clienteEmail: string; status: string };
const categorias = ['CAMISA', 'CAMISETA', 'MOLETOM', 'CALCA', 'ACESSORIO'];
const status = ['PENDENTE', 'EM_CORTE', 'EM_COSTURA', 'ACABAMENTO', 'CONCLUIDO'];
const vazio = { nome: '', descricao: '', preco: '', categoria: 'CAMISETA', tipo: '' };
const dinheiro = (valor: number) => `R$ ${Number(valor).toFixed(2).replace('.', ',')}`;

function erroApi(error: unknown) {
    if (isAxiosError(error)) {
        if (error.response?.status === 403) return 'Sua conta não tem permissão para esta operação.';
        if (error.response?.status === 401) return 'Sessão expirada. Entre novamente.';
        const mensagem = error.response?.data?.message ?? error.response?.data?.erro ?? error.response?.data?.error;
        if (typeof mensagem === 'string') return mensagem;
        if (error.response?.status === 405) return 'Esta operação precisa da versão atualizada do backend. Reinicie o serviço após aplicar a atualização.';
        if ((error.response?.status ?? 0) >= 500) return 'O serviço de administração falhou. Verifique os logs do backend e tente novamente após recuperar o serviço.';
        if (!error.response) return 'O backend não respondeu. Verifique se o gateway e o serviço de customizações estão em execução.';
    }
    return error instanceof Error && !isAxiosError(error) ? error.message : 'Não foi possível concluir. Verifique a conexão e tente novamente.';
}

function Botao({ titulo, onPress, disabled = false, perigo = false }: { titulo: string; onPress: () => void; disabled?: boolean; perigo?: boolean }) {
    return <Pressable accessibilityRole="button" accessibilityState={{ disabled }} disabled={disabled} onPress={onPress}
        style={[styles.botao, perigo && styles.perigo, disabled && { opacity: 0.5 }]}><Text style={styles.botaoTexto}>{titulo}</Text></Pressable>;
}

export default function AdminGestao({ secao }: { secao: 'Produtos' | 'Customizações' | 'Produção' }) {
    const [produtos, setProdutos] = useState<Produto[]>([]);
    const [opcoes, setOpcoes] = useState<Opcao[]>([]);
    const [ordens, setOrdens] = useState<Ordem[]>([]);
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
                const { data } = await api.get<Ordem[]>('/v1/producao');
                if (atual === revisao.current) setOrdens(data);
            } else {
                const { data } = await api.get<Produto[]>('/v1/produtos');
                if (atual !== revisao.current) return;
                setProdutos(data);
                if (secao === 'Customizações' && produtoId) {
                    const resposta = await api.get<Opcao[]>(`/v1/customizacoes/produto/${produtoId}`);
                    if (atual === revisao.current) setOpcoes(resposta.data.filter(item => item.ativo));
                } else setOpcoes([]);
            }
        } catch (error) { if (atual === revisao.current) setErro(erroApi(error)); }
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
        catch (error) { setErro(erroApi(error)); }
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
            const caminho = secao === 'Produtos' ? '/v1/produtos' : '/v1/customizacoes';
            const dados = secao === 'Produtos'
                ? { nome: form.nome.trim(), descricao: form.descricao.trim(), precoBase: preco, categoria: form.categoria }
                : { produtoId, nome: form.nome.trim(), tipo: form.tipo.trim(), precoAdicional: preco };
            if (editando !== null) await api.put(`${caminho}/${editando}`, dados);
            else await api.post(caminho, dados);
            setAberto(false); setEditando(null); setForm(vazio);
        }, 'Registro salvo com sucesso.');
    }
    const campo = (chave: keyof typeof vazio, label: string, maxLength?: number) => <View style={{ gap: 6 }}>
        <Text style={styles.label}>{label}</Text>
        <TextInput accessibilityLabel={label} value={form[chave]} editable={!busy} maxLength={maxLength}
            keyboardType={chave === 'preco' ? 'decimal-pad' : 'default'} multiline={chave === 'descricao'}
            onChangeText={valor => setForm(atual => ({ ...atual, [chave]: valor }))} style={styles.input} />
    </View>;

    return <View style={{ gap: 14 }}>
        <View style={styles.linha}><Text style={styles.titulo}>{secao}</Text><Botao titulo="Atualizar" disabled={busy || carregando} onPress={() => void carregar()} /></View>
        {erro ? <Text accessibilityRole="alert" style={styles.erro}>{erro}</Text> : null}
        {sucesso ? <Text style={styles.sucesso}>{sucesso}</Text> : null}
        {secao === 'Customizações' && <>
            <Text style={styles.label}>Selecione o produto</Text>
            <ScrollView horizontal><View style={styles.linha}>{produtos.map(p => <Pressable key={p.id} disabled={busy} accessibilityRole="button" accessibilityState={{ selected: produtoId === p.id }}
                onPress={() => { setProdutoId(p.id); setOpcoes([]); setAberto(false); setExcluir(null); setSucesso(''); }}
                style={[styles.chip, produtoId === p.id && styles.selecionado]}><Text>{p.nome}</Text></Pressable>)}</View></ScrollView>
            {!produtoId && <Text>Escolha um produto para gerenciar suas opções.</Text>}
        </>}
        {secao !== 'Produção' && <Botao titulo={secao === 'Produtos' ? 'Cadastrar produto' : 'Cadastrar customização'} disabled={busy || carregando || (secao === 'Customizações' && !produtoId)} onPress={novo} />}
        {aberto && <View style={styles.card}>
            <Text style={styles.titulo}>{editando === null ? 'Novo cadastro' : `Editar #${editando}`}</Text>
            {campo('nome', 'Nome', 160)}
            {secao === 'Produtos' ? <>{campo('descricao', 'Descrição', 255)}<Text style={styles.label}>Categoria</Text><View style={styles.linha}>{categorias.map(c => <Pressable key={c} disabled={busy} accessibilityRole="button" accessibilityState={{ selected: form.categoria === c }} onPress={() => setForm(atual => ({ ...atual, categoria: c }))} style={[styles.chip, form.categoria === c && styles.selecionado]}><Text>{c}</Text></Pressable>)}</View></> : campo('tipo', 'Tipo (ex.: COR, ESTAMPA)', 80)}
            {campo('preco', secao === 'Produtos' ? 'Preço base (R$)' : 'Preço adicional (R$)')}
            <View style={styles.linha}><Botao titulo="Salvar" disabled={busy} onPress={() => void salvar()} /><Botao titulo="Cancelar" disabled={busy} onPress={() => { setAberto(false); setErro(''); }} /></View>
        </View>}
        {(busy || carregando) && <ActivityIndicator color="#334B35" />}
        {!carregando && secao === 'Produtos' && !produtos.length && <Text>Nenhum produto ativo. Cadastre o primeiro produto.</Text>}
        {!carregando && secao === 'Customizações' && produtoId && !opcoes.length && <Text>Nenhuma customização ativa para este produto.</Text>}
        {(secao === 'Produtos' ? produtos : secao === 'Customizações' ? opcoes : []).map(item => <View key={item.id} style={styles.card}>
            <Text style={styles.titulo}>{item.nome}</Text>
            {'precoBase' in item ? <Text>{item.categoria} · {dinheiro(item.precoBase)}{'\n'}{item.descricao}</Text> : <Text>{item.tipo} · {dinheiro(item.precoAdicional)}</Text>}
            <View style={styles.linha}><Botao titulo="Editar" disabled={busy || carregando} onPress={() => {
                setEditando(item.id); setExcluir(null); setAberto(true); setErro(''); setSucesso('');
                setForm('precoBase' in item ? { ...vazio, nome: item.nome, descricao: item.descricao ?? '', categoria: item.categoria, preco: String(item.precoBase) } : { ...vazio, nome: item.nome, tipo: item.tipo, preco: String(item.precoAdicional) });
            }} /><Botao titulo="Excluir" perigo disabled={busy || carregando} onPress={() => setExcluir(item.id)} /></View>
            {excluir === item.id && <><Text>Excluir “{item.nome}”? O registro será inativado e deixará de aparecer nas opções disponíveis.</Text><View style={styles.linha}>
                <Botao titulo="Confirmar exclusão" perigo disabled={busy} onPress={() => void executar(async () => {
                    await api.delete(`${secao === 'Produtos' ? '/v1/produtos' : '/v1/customizacoes'}/${item.id}`);
                    setExcluir(null); if (editando === item.id) setAberto(false);
                }, 'Registro excluído das opções disponíveis.')} /><Botao titulo="Cancelar" disabled={busy} onPress={() => setExcluir(null)} />
            </View></>}
        </View>)}
        {secao === 'Produção' && <>
            {!carregando && !ordens.length && <Text>Nenhuma ordem de produção disponível.</Text>}
            {ordens.map(ordem => <View key={ordem.id} style={styles.card}><Text style={styles.titulo}>Ordem #{ordem.id} · Pedido #{ordem.pedidoId}</Text><Text>{ordem.clienteEmail}</Text><Text>Status: {ordem.status}</Text>
                <Text style={styles.label}>Alterar status</Text><View style={styles.linha}>{status.map(s => <Botao key={s} titulo={s.replaceAll('_', ' ')} disabled={busy || carregando || ordem.status === s} onPress={() => void executar(async () => { await api.patch(`/v1/producao/${ordem.id}/status`, { status: s }); }, 'Status atualizado com sucesso.')} />)}</View>
            </View>)}
        </>}
    </View>;
}

const styles = StyleSheet.create({
    titulo: { fontSize: 18, fontWeight: '700', color: '#202522' },
    label: { fontWeight: '600', color: '#334B35' },
    linha: { flexDirection: 'row', flexWrap: 'wrap', gap: 8, alignItems: 'center' },
    card: { padding: 16, backgroundColor: 'white', borderRadius: 8, borderWidth: 1, borderColor: '#E2E6E1', gap: 12 },
    input: { borderWidth: 1, borderColor: '#A6B0A6', borderRadius: 5, padding: 12, color: '#202522', backgroundColor: 'white' },
    botao: { padding: 12, minHeight: 44, backgroundColor: '#334B35', borderRadius: 5, justifyContent: 'center' },
    botaoTexto: { color: 'white', fontWeight: '600' },
    perigo: { backgroundColor: '#9B3030' },
    chip: { padding: 12, minHeight: 44, borderWidth: 1, borderColor: '#A6B0A6', borderRadius: 5 },
    selecionado: { backgroundColor: '#DCE8D8', borderColor: '#334B35' },
    erro: { color: '#9B3030' },
    sucesso: { color: '#334B35' },
});
