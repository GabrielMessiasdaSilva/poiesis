import { useCallback, useRef, useState } from 'react';
import { ActivityIndicator, Pressable, ScrollView, StyleSheet, Text, View, useWindowDimensions } from 'react-native';
import { useFocusEffect } from 'expo-router';
import { Feather } from '@expo/vector-icons';
import { produtosService, customizacoesService, categorias, type Categoria, type Produto, type OpcaoCustomizacao } from '../services/produtos';
import { producaoService, type OrdemProducao } from '../services/producao';
import { mensagemApi } from '../services/erros';
import { Button, Field, Notice } from '../ui/components';
import { colors, serif } from '../ui/theme';

const vazio = { nome: '', descricao: '', preco: '', categoria: 'CAMISETA' as Categoria, tipo: 'COR', tamanhos: 'P, M, G, GG', cores: 'Off-white, Preto', elementos: 'Estrela, Clássico, Coração' };
const tiposCustomizacao = [
    { tipo: 'COR', rotulo: 'Cor da peça' },
    { tipo: 'TAMANHO', rotulo: 'Tamanho' },
    { tipo: 'ESTAMPA_GRAFICA', rotulo: 'Estampa gráfica' },
    { tipo: 'COR_IMPRESSAO', rotulo: 'Cor da impressão' },
    { tipo: 'TEXTO_PERSONALIZADO', rotulo: 'Texto personalizado' },
    { tipo: 'OUTRO', rotulo: 'Outra opção' },
] as const;
const tipoNormalizado = (tipo: string) => tipo.trim().toLocaleUpperCase('pt-BR').normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/[\s-]/g, '_');
const tipoDaCustomizacao = (tipo: string) => {
    const valor = tipoNormalizado(tipo);
    if (['COR', 'COLOR', 'COR_DA_PECA'].includes(valor)) return 'COR';
    if (['TAMANHO', 'SIZE'].includes(valor)) return 'TAMANHO';
    if (['ESTAMPAGRAFICA', 'ESTAMPA_GRAFICA', 'ESTAMPA', 'GRAFICA'].includes(valor)) return 'ESTAMPA_GRAFICA';
    if (['CORIMPRESSAO', 'COR_IMPRESSAO', 'TINTA', 'INK'].includes(valor)) return 'COR_IMPRESSAO';
    if (['TEXTO', 'TEXTO_PERSONALIZADO', 'PERSONALIZACAO_DE_TEXTO'].includes(valor)) return 'TEXTO_PERSONALIZADO';
    return 'OUTRO';
};
const dinheiro = (valor: number) => `R$ ${Number(valor).toFixed(2).replace('.', ',')}`;
const proximaEtapa = (status: OrdemProducao['status']) => ({
    PENDENTE: 'APROVADO', APROVADO: 'EM_CORTE', EM_CORTE: 'EM_COSTURA',
    EM_COSTURA: 'EM_PERSONALIZACAO', EM_PERSONALIZACAO: 'FINALIZADO', FINALIZADO: 'ENTREGUE',
} as Partial<Record<OrdemProducao['status'], OrdemProducao['status']>>)[status];

function Botao({ titulo, onPress, disabled = false, perigo = false }: { titulo: string; onPress: () => void; disabled?: boolean; perigo?: boolean }) {
    return <Button title={titulo} onPress={onPress} disabled={disabled} danger={perigo} secondary={titulo === 'Cancelar' || titulo === 'Atualizar' || titulo === 'Editar'} />;
}

export default function AdminGestao({ secao }: { secao: 'Produtos' | 'Customizações' | 'Produção' }) {
    const { width } = useWindowDimensions();
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
    const [fraseAtiva, setFraseAtiva] = useState(true);
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

    function novo() { setForm(vazio); setFraseAtiva(true); setEditando(null); setAberto(true); setExcluir(null); setErro(''); setSucesso(''); }
    const separarOpcoes = (texto: string) => [...new Set(texto.split(/[\n,;]/).map(valor => valor.trim()).filter(Boolean))];
    const chaveOpcao = (nome: string) => nome.trim().toLocaleLowerCase('pt-BR').normalize('NFD').replace(/[\u0300-\u036f]/g, '');
    async function sincronizarOpcoes(produtoId: number) {
        const existentes = await customizacoesService.listarPorProduto(produtoId);
        const grupos = [
            { tipo: 'COR', nomes: separarOpcoes(form.cores) },
            { tipo: 'TAMANHO', nomes: separarOpcoes(form.tamanhos) },
            { tipo: 'ESTAMPA_GRAFICA', nomes: separarOpcoes(form.elementos) },
            { tipo: 'TEXTO_PERSONALIZADO', nomes: fraseAtiva ? ['Frase personalizada'] : [] },
        ];
        for (const grupo of grupos) {
            const atuais = existentes.filter(opcao => tipoDaCustomizacao(opcao.tipo) === grupo.tipo);
            const usados = new Set<number>();
            for (const nome of grupo.nomes) {
                const existente = atuais.find(opcao => !usados.has(opcao.id) && chaveOpcao(opcao.nome) === chaveOpcao(nome));
                if (existente) {
                    usados.add(existente.id);
                    if (existente.nome !== nome || Number(existente.precoAdicional) !== 0) await customizacoesService.atualizar(existente.id, { produtoId, nome, tipo: grupo.tipo, precoAdicional: 0 });
                } else await customizacoesService.cadastrar({ produtoId, nome, tipo: grupo.tipo, precoAdicional: 0 });
            }
            for (const opcao of atuais) if (!usados.has(opcao.id)) await customizacoesService.inativar(opcao.id);
        }
    }
    async function editarProduto(produto: Produto) {
        setErro(''); setSucesso(''); setCarregando(true);
        setForm({ ...vazio, nome: produto.nome, descricao: produto.descricao ?? '', categoria: produto.categoria, preco: String(produto.precoBase) });
        setEditando(produto.id); setExcluir(null); setAberto(false);
        try {
            const opcoesAtuais = await customizacoesService.listarPorProduto(produto.id);
            const nomes = (tipo: string) => opcoesAtuais.filter(opcao => tipoDaCustomizacao(opcao.tipo) === tipo).map(opcao => opcao.nome).join(', ');
            setForm({ ...vazio, nome: produto.nome, descricao: produto.descricao ?? '', categoria: produto.categoria, preco: String(produto.precoBase), cores: nomes('COR'), tamanhos: nomes('TAMANHO'), elementos: nomes('ESTAMPA_GRAFICA') });
            setFraseAtiva(opcoesAtuais.some(opcao => tipoDaCustomizacao(opcao.tipo) === 'TEXTO_PERSONALIZADO'));
            setAberto(true);
        } catch (error) { setErro(mensagemApi(error)); setEditando(null); }
        finally { setCarregando(false); }
    }
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
        if (secao === 'Customizações' && (!produtoId || !tiposCustomizacao.some(item => item.tipo === form.tipo))) { setErro('Selecione um produto e uma categoria de customização.'); return; }
        await executar(async () => {
            if (secao === 'Produtos') {
                const dados = { nome: form.nome.trim(), descricao: form.descricao.trim(), precoBase: preco, categoria: form.categoria };
                let produtoId = editando;
                if (produtoId !== null) await produtosService.atualizar(produtoId, dados);
                else {
                    const produtoCriado = await produtosService.cadastrar(dados);
                    produtoId = produtoCriado.id;
                    setEditando(produtoId);
                }
                try { await sincronizarOpcoes(produtoId); }
                catch (error) { throw new Error(`Produto salvo, mas não foi possível salvar todas as variações: ${mensagemApi(error)}. Edite o produto para tentar novamente.`); }
            } else {
                if (produtoId === null) throw new Error('Selecione um produto.');
                const dados = { produtoId, nome: form.nome.trim(), tipo: form.tipo.trim(), precoAdicional: preco };
                if (editando !== null) await customizacoesService.atualizar(editando, dados);
                else await customizacoesService.cadastrar(dados);
            }
            setAberto(false); setEditando(null); setForm(vazio);
        }, 'Registro salvo com sucesso.');
    }
    const campo = (chave: keyof typeof vazio, label: string, maxLength?: number, hint?: string) => <Field label={label} hint={hint} value={form[chave]} editable={!busy} maxLength={maxLength}
        keyboardType={chave === 'preco' ? 'decimal-pad' : 'default'} multiline={['descricao', 'cores', 'tamanhos', 'elementos'].includes(chave)}
        onChangeText={valor => setForm(atual => ({ ...atual, [chave]: valor }))} />;

    const descricao = secao === 'Produtos' ? 'Cadastre peças, mantenha os preços atualizados e controle o que aparece no catálogo.' : secao === 'Customizações' ? 'Defina as opções disponíveis para cada peça, como cor, tamanho e estampa.' : 'Acompanhe os pedidos em produção e avance cada um conforme o trabalho evolui.';
    const labelNovo = secao === 'Produtos' ? 'Cadastrar produto' : 'Cadastrar customização';
    const etapas = ['PENDENTE', 'APROVADO', 'EM_CORTE', 'EM_COSTURA', 'EM_PERSONALIZACAO', 'FINALIZADO', 'ENTREGUE'] as const;
    const rotuloEtapa = (etapa: string) => ({ PENDENTE: 'Pendente', APROVADO: 'Aprovado', EM_CORTE: 'Corte', EM_COSTURA: 'Costura', EM_PERSONALIZACAO: 'Personalização', FINALIZADO: 'Finalizado', ENTREGUE: 'Entregue', CANCELADO: 'Cancelado' } as Record<string, string>)[etapa] ?? etapa;
    return <View style={{ gap: 16 }}>
        <View style={[styles.intro, width >= 760 && styles.introWide]}>
            <View style={{ flex: 1, gap: 5 }}><Text style={styles.titulo}>{secao === 'Produtos' ? 'Catálogo de produtos' : secao === 'Customizações' ? 'Opções de personalização' : 'Fila de produção'}</Text><Text style={styles.descricao}>{descricao}</Text></View>
            <View style={styles.linha}>
                {secao !== 'Produção' && <Button title={labelNovo} icon="plus" disabled={busy || carregando || (secao === 'Customizações' && !produtoId)} onPress={novo} />}
                <Button title="Atualizar" icon="refresh-cw" secondary disabled={busy || carregando} onPress={() => void carregar()} />
            </View>
        </View>
        {erro ? <Notice error>{erro}</Notice> : null}
        {sucesso ? <Notice>{sucesso}</Notice> : null}
        {secao === 'Customizações' && <>
            <View style={styles.card}><Text style={styles.label}>Produto</Text><Text style={styles.descricao}>Selecione a peça para ver e editar suas opções.</Text>
            {produtos.length ? <ScrollView horizontal showsHorizontalScrollIndicator={false}><View style={styles.linha}>{produtos.map(p => <Pressable key={p.id} disabled={busy} accessibilityRole="button" accessibilityState={{ selected: produtoId === p.id }}
                onPress={() => { setProdutoId(p.id); setOpcoes([]); setAberto(false); setExcluir(null); setSucesso(''); }}
                style={[styles.chip, produtoId === p.id && styles.selecionado]}><Text style={{ color: produtoId === p.id ? colors.primary : colors.ink, fontWeight: produtoId === p.id ? '700' : '500' }}>{p.nome}</Text></Pressable>)}</View></ScrollView> : <Text style={styles.vazio}>Cadastre um produto antes de criar opções.</Text>}
            </View>
        </>}
        {aberto && <View style={styles.card}>
            <View style={styles.formTitulo}><View style={{ flex: 1, gap: 4 }}><Text style={styles.titulo}>{editando === null ? 'Novo cadastro' : `Editar registro #${editando}`}</Text><Text style={styles.descricao}>Preencha os dados e salve para atualizar o catálogo.</Text></View><Pressable accessibilityRole="button" accessibilityLabel="Fechar formulário" disabled={busy} onPress={() => { setAberto(false); setErro(''); }} style={styles.fechar}><Text style={{ color: colors.muted, fontSize: 18 }}>×</Text></Pressable></View>
            {campo('nome', secao === 'Produtos' ? 'Nome do produto' : 'Nome da opção', 160)}
            {secao === 'Produtos' ? <><Text style={styles.label}>Categoria do produto</Text><View style={styles.linha}>{categorias.map(c => <Pressable key={c} disabled={busy} accessibilityRole="button" accessibilityState={{ selected: form.categoria === c }} onPress={() => setForm(atual => ({ ...atual, categoria: c }))} style={[styles.chip, form.categoria === c && styles.selecionado]}><Text style={{ color: colors.primary }}>{c}</Text></Pressable>)}</View>{campo('descricao', 'Descrição', 255)}<View style={styles.divisorForm} /><Text style={styles.secaoForm}>Variações do produto</Text><Text style={styles.descricao}>Tamanhos e cores têm o mesmo preço base do produto.</Text>{campo('tamanhos', 'Tamanhos disponíveis', 500, 'Separe por vírgulas ou linhas. Ex.: P, M, G, GG.')}{campo('cores', 'Cores disponíveis', 500, 'Separe por vírgulas ou linhas. Ex.: Off-white, Preto, Lavanda.')}{campo('elementos', 'Elementos gráficos', 500, 'Use nomes como Estrela, Clássico, Coração ou Sem estampa.') }<Pressable accessibilityRole="checkbox" accessibilityState={{ checked: fraseAtiva }} onPress={() => setFraseAtiva(value => !value)} style={styles.checkLinha}><View style={[styles.checkbox, fraseAtiva && styles.checkboxAtivo]}>{fraseAtiva && <Feather name="check" size={13} color={colors.paper} />}</View><View style={{ flex: 1, gap: 3 }}><Text style={styles.label}>Permitir frase personalizada</Text><Text style={styles.descricao}>O cliente poderá incluir uma frase curta no editor.</Text></View></Pressable></> : <><Text style={styles.label}>Categoria da opção</Text><Text style={styles.descricao}>O cliente verá esta categoria ao personalizar o produto.</Text><View style={styles.linha}>{tiposCustomizacao.map(tipo => <Pressable key={tipo.tipo} disabled={busy} accessibilityRole="button" accessibilityState={{ selected: form.tipo === tipo.tipo }} onPress={() => setForm(atual => ({ ...atual, tipo: tipo.tipo }))} style={[styles.chip, form.tipo === tipo.tipo && styles.selecionado]}><Text style={{ color: form.tipo === tipo.tipo ? colors.primary : colors.ink, fontWeight: form.tipo === tipo.tipo ? '700' : '500' }}>{tipo.rotulo}</Text></Pressable>)}</View><Text style={styles.descricao}>{form.tipo === 'ESTAMPA_GRAFICA' ? 'Use Estrela, Clássico, Coração ou Sem estampa para atualizar a prévia.' : form.tipo === 'COR_IMPRESSAO' ? 'Use Grafite, Branco, Lavanda, Coral ou um hexadecimal, como #8E729D.' : form.tipo === 'COR' ? 'Informe uma cor, como Preto, Branco, Azul ou Lavanda.' : form.tipo === 'TAMANHO' ? 'Informe o tamanho, como P, M, G ou GG.' : form.tipo === 'TEXTO_PERSONALIZADO' ? 'Ao selecionar esta opção, o cliente poderá digitar um texto.' : 'Opção adicional disponível para este produto.'}</Text></>}
            {campo('preco', secao === 'Produtos' ? 'Preço base (R$)' : 'Preço adicional (R$)')}
            <View style={styles.linha}><Botao titulo="Salvar" disabled={busy} onPress={() => void salvar()} /><Botao titulo="Cancelar" disabled={busy} onPress={() => { setAberto(false); setErro(''); }} /></View>
        </View>}
        {(busy || carregando) && <ActivityIndicator color={colors.primary} />}
        {!carregando && secao === 'Produtos' && !produtos.length && <View style={styles.estadoVazio}><Text style={styles.vazioTitulo}>Seu catálogo está vazio</Text><Text style={styles.descricao}>Cadastre o primeiro produto para começar.</Text></View>}
        {!carregando && secao === 'Customizações' && produtoId && !opcoes.length && <View style={styles.estadoVazio}><Text style={styles.vazioTitulo}>Nenhuma opção cadastrada</Text><Text style={styles.descricao}>Adicione cores, tamanhos ou técnicas para este produto.</Text></View>}
        {!carregando && secao !== 'Produção' && (secao === 'Produtos' ? produtos.length : produtoId ? opcoes.length : 0) > 0 && <Text style={styles.contagem}>{secao === 'Produtos' ? `${produtos.length} ${produtos.length === 1 ? 'produto' : 'produtos'} no catálogo` : `${opcoes.length} ${opcoes.length === 1 ? 'opção' : 'opções'} para este produto`}</Text>}
        {(secao === 'Produtos' ? produtos : secao === 'Customizações' ? opcoes : []).map(item => <View key={item.id} style={styles.card}>
            <View style={styles.itemCabecalho}><View style={{ flex: 1, gap: 5 }}><Text style={styles.titulo}>{item.nome}</Text><Text style={styles.descricao}>{'precoBase' in item ? `${item.categoria.replaceAll('_', ' ')}${item.descricao ? ` · ${item.descricao}` : ''}` : item.tipo.replaceAll('_', ' ')}</Text></View><View style={styles.precoTag}><Text style={styles.preco}>{dinheiro('precoBase' in item ? item.precoBase : item.precoAdicional)}</Text><Text style={styles.precoLegenda}>{'precoBase' in item ? 'preço base' : 'adicional'}</Text></View></View>
            <View style={styles.itemAcoes}><Button title="Editar" icon="edit-2" secondary disabled={busy || carregando} onPress={() => {
                if ('precoBase' in item) void editarProduto(item);
                else { setEditando(item.id); setExcluir(null); setAberto(true); setErro(''); setSucesso(''); setForm({ ...vazio, nome: item.nome, tipo: tipoDaCustomizacao(item.tipo), preco: String(item.precoAdicional) }); }
            }} /><Button title="Inativar" icon="trash-2" danger disabled={busy || carregando} onPress={() => setExcluir(item.id)} /></View>
            {excluir === item.id && <View style={styles.confirmacao}><Text style={styles.descricao}>Inativar “{item.nome}”? Ele deixará de aparecer nas opções disponíveis.</Text><View style={styles.linha}>
                <Botao titulo="Confirmar exclusão" perigo disabled={busy} onPress={() => void executar(async () => {
                    if (secao === 'Produtos') await produtosService.inativar(item.id);
                    else await customizacoesService.inativar(item.id);
                    setExcluir(null); if (editando === item.id) setAberto(false);
                }, 'Registro excluído das opções disponíveis.')} /><Botao titulo="Cancelar" disabled={busy} onPress={() => setExcluir(null)} />
            </View></View>}
        </View>)}
        {secao === 'Produção' && <>
            {!carregando && !ordens.length && <View style={styles.estadoVazio}><Text style={styles.vazioTitulo}>Nenhum pedido em produção</Text><Text style={styles.descricao}>Quando houver pedidos, eles aparecerão aqui para acompanhamento.</Text></View>}
            {!!ordens.length && <View style={[styles.linha, { alignItems: 'stretch' }]}>{[
                ['Em andamento', ordens.filter(o => !['ENTREGUE', 'CANCELADO'].includes(o.status)).length],
                ['Concluídos', ordens.filter(o => o.status === 'ENTREGUE').length],
                ['Cancelados', ordens.filter(o => o.status === 'CANCELADO').length],
            ].map(([titulo, total]) => <View key={titulo} style={styles.resumo}><Text style={styles.descricao}>{titulo}</Text><Text style={styles.resumoNumero}>{total}</Text></View>)}</View>}
            {ordens.map(ordem => {
                const proxima = proximaEtapa(ordem.status);
                const encerrada = ['ENTREGUE', 'CANCELADO'].includes(ordem.status);
                const etapaAtual = etapas.indexOf(ordem.status as typeof etapas[number]);
                return <View key={ordem.id} style={styles.ordemCard}>
                    <View style={styles.ordemCabecalho}>
                        <View style={{ flex: 1, gap: 4 }}><Text style={styles.titulo}>Pedido #{ordem.pedidoId}</Text><Text style={styles.cliente}>{ordem.clienteEmail}</Text><Text style={styles.cliente}>Atualizado em {new Date(ordem.dataAtualizacao).toLocaleDateString('pt-BR')}</Text></View>
                        <View style={[styles.status, ordem.status === 'ENTREGUE' && styles.statusConcluido, ordem.status === 'CANCELADO' && styles.statusCancelado]}><Text style={styles.statusTexto}>{rotuloEtapa(ordem.status)}</Text></View>
                    </View>
                    {ordem.status !== 'CANCELADO' && <View style={{ gap: 10 }}><Text style={styles.label}>Progresso do pedido</Text><ScrollView horizontal showsHorizontalScrollIndicator={false}><View style={styles.etapas}>{etapas.map((etapa, index) => <View key={etapa} style={styles.etapaItem}><View style={[styles.etapaPonto, etapaAtual >= 0 && index < etapaAtual && styles.etapaConcluida, etapa === ordem.status && styles.etapaAtual]}><Text style={[styles.etapaNumero, ((etapaAtual >= 0 && index < etapaAtual) || etapa === ordem.status) && styles.pontoTextoAtual]}>{etapaAtual >= 0 && index < etapaAtual ? '✓' : index + 1}</Text></View><Text style={[styles.pontoTexto, etapa === ordem.status && styles.etapaTextoAtual]}>{rotuloEtapa(etapa)}</Text></View>)}</View></ScrollView></View>}
                    {!encerrada && <View style={styles.proximaAcao}><View style={{ flex: 1, gap: 3 }}><Text style={styles.label}>Próxima etapa</Text><Text style={styles.descricao}>{proxima ? rotuloEtapa(proxima) : 'Pedido encerrado'}</Text></View><View style={styles.linha}>
                        {proxima && <Button title={`Avançar · ${rotuloEtapa(proxima)}`} icon="arrow-right" disabled={busy || carregando} onPress={() => void executar(async () => { await producaoService.atualizarStatus(ordem.id, proxima); }, 'Etapa de produção atualizada.')}/>}
                        <Botao titulo="Cancelar produção" perigo disabled={busy || carregando} onPress={() => void executar(async () => { await producaoService.atualizarStatus(ordem.id, 'CANCELADO'); }, 'Produção cancelada.')}/>
                    </View></View>}
                </View>;
            })}
        </>}
    </View>;
}

const styles = StyleSheet.create({
    titulo: { fontSize: 23, fontFamily: serif, color: colors.ink },
    intro: { gap: 14, padding: 18, borderRadius: 16, borderWidth: 1, borderColor: colors.line, backgroundColor: colors.paper },
    introWide: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' },
    descricao: { color: colors.muted, fontSize: 13, lineHeight: 19 },
    contagem: { color: colors.muted, fontSize: 12, fontWeight: '600' },
    estadoVazio: { padding: 24, borderWidth: 1, borderColor: colors.line, borderStyle: 'dashed', borderRadius: 14, alignItems: 'center', gap: 7, backgroundColor: colors.paper },
    vazioTitulo: { color: colors.ink, fontSize: 17, fontWeight: '700' },
    vazio: { color: colors.muted, fontSize: 13 },
    label: { fontWeight: '600', color: colors.primary },
    linha: { flexDirection: 'row', flexWrap: 'wrap', gap: 8, alignItems: 'center' },
    card: { padding: 22, backgroundColor: colors.paper, borderRadius: 14, borderWidth: 1, borderColor: colors.line, gap: 12 },
    formTitulo: { flexDirection: 'row', alignItems: 'flex-start', gap: 12 },
    secaoForm: { color: colors.ink, fontSize: 17, fontWeight: '700' },
    divisorForm: { height: 1, backgroundColor: colors.line, marginVertical: 4 },
    checkLinha: { flexDirection: 'row', alignItems: 'center', gap: 11, padding: 14, borderRadius: 12, borderWidth: 1, borderColor: colors.line, backgroundColor: colors.background },
    checkbox: { width: 22, height: 22, borderRadius: 6, borderWidth: 1, borderColor: colors.line, alignItems: 'center', justifyContent: 'center', backgroundColor: colors.paper },
    checkboxAtivo: { backgroundColor: colors.primary, borderColor: colors.primary },
    fechar: { width: 36, height: 36, borderRadius: 18, backgroundColor: colors.background, alignItems: 'center', justifyContent: 'center' },
    itemCabecalho: { flexDirection: 'row', alignItems: 'center', gap: 14 },
    itemAcoes: { flexDirection: 'row', flexWrap: 'wrap', gap: 8, paddingTop: 12, borderTopWidth: 1, borderTopColor: colors.line },
    precoTag: { paddingHorizontal: 12, paddingVertical: 9, borderRadius: 10, backgroundColor: colors.background, alignItems: 'flex-end' },
    preco: { color: colors.ink, fontSize: 16, fontWeight: '700' },
    precoLegenda: { color: colors.muted, fontSize: 10 },
    confirmacao: { padding: 14, borderRadius: 12, backgroundColor: colors.errorBg, gap: 10 },
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
    statusConcluido: { backgroundColor: colors.successBg },
    statusCancelado: { backgroundColor: colors.errorBg },
    statusTexto: { color: colors.primary, fontSize: 11, fontWeight: '700', letterSpacing: .4 },
    resumo: { flex: 1, minWidth: 135, padding: 16, borderRadius: 13, backgroundColor: colors.paper, borderWidth: 1, borderColor: colors.line, gap: 5 },
    resumoNumero: { color: colors.ink, fontSize: 25, fontWeight: '700' },
    etapas: { flexDirection: 'row', gap: 0, paddingVertical: 3 },
    etapaItem: { minWidth: 92, alignItems: 'center', gap: 7 },
    etapaPonto: { width: 28, height: 28, borderRadius: 14, alignItems: 'center', justifyContent: 'center', backgroundColor: colors.background, borderWidth: 1, borderColor: colors.line },
    etapaConcluida: { backgroundColor: colors.successBg, borderColor: colors.success },
    etapaAtual: { backgroundColor: colors.primary, borderColor: colors.primary },
    etapaNumero: { color: colors.muted, fontSize: 11, fontWeight: '700' },
    etapaTextoAtual: { color: colors.primary, fontWeight: '700' },
    proximaAcao: { flexDirection: 'row', flexWrap: 'wrap', alignItems: 'center', justifyContent: 'space-between', gap: 12, padding: 14, backgroundColor: colors.background, borderRadius: 12 },
    pontoEtapa: { paddingHorizontal: 9, paddingVertical: 6, borderRadius: 14, backgroundColor: colors.background },
    pontoEtapaAtual: { backgroundColor: colors.primary },
    pontoTexto: { color: colors.muted, fontSize: 10, fontWeight: '600' },
    pontoTextoAtual: { color: colors.paper },
});
