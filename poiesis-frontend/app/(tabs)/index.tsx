import { useContext, useRef, useState } from 'react';
import { ActivityIndicator, Image, Pressable, ScrollView, Text, View, useWindowDimensions } from 'react-native';
import { router } from 'expo-router';
import { Feather, MaterialCommunityIcons } from '@expo/vector-icons';
import { produtosService, customizacoesService, type Produto, type OpcaoCustomizacao } from '../../src/services/produtos';
import { pedidosService, type Pedido } from '../../src/services/pedidos';
import { mensagemApi } from '../../src/services/erros';
import { AuthContext } from '../../src/contexts/AuthContext';
import { useConsulta } from '../../src/hooks/useConsulta';
import { Button, EmptyState, Field, Notice, PageHeader, Screen } from '../../src/ui/components';
import { colors, money, serif, ui } from '../../src/ui/theme';

const categorias: Record<string, string> = { CAMISA: 'Camisas', CAMISETA: 'Camisetas', MOLETOM: 'Moletons', CALCA: 'Calças', ACESSORIO: 'Acessórios' };
const corDaOpcao = (nome: string) => /^#[0-9a-f]{6}$/i.test(nome.trim()) ? nome.trim() : ({
    branco: '#FBF8F2', preto: '#151515', grafite: '#38281F', azul: '#1F5FAF', vermelho: '#C9362B', coral: '#D95A5A', verde: '#36724D', amarelo: '#E7B82C', rosa: '#D95A91', laranja: '#E78131', cinza: '#8B8D90', marrom: '#704A35', bege: '#D8C5A8', roxo: '#6C4B8E', lavanda: '#8E729D', azulmarinho: '#132C67', vinho: '#702331', turquesa: '#26AFC4', offwhite: '#EEE9DF',
}[nome.toLocaleLowerCase('pt-BR').replace(/[\s-]/g, '')] ?? colors.paper);
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
const rotuloTipo = (tipo: string) => ({ COR: 'Cor da peça', TAMANHO: 'Tamanho', ESTAMPA_GRAFICA: 'Estampa gráfica', COR_IMPRESSAO: 'Cor da impressão', TEXTO_PERSONALIZADO: 'Texto personalizado', OUTRO: 'Outra opção' }[tipoDaCustomizacao(tipo)] ?? tipo.replaceAll('_', ' '));
const simboloDaOpcao = (nome: string) => {
    const valor = nome.trim().toLocaleLowerCase('pt-BR').normalize('NFD').replace(/[\u0300-\u036f]/g, '');
    if (valor.includes('sem estampa') || valor.includes('nenhuma')) return '';
    if (valor.includes('coracao')) return '♥';
    if (valor.includes('classico') || valor.includes('estrela cheia')) return '★';
    if (valor.includes('flor')) return '❀';
    if (valor.includes('circulo')) return '●';
    if (valor.includes('estrela')) return '✦';
    return [...nome.trim()][0] ?? '';
};
function CartaoProduto({ produto }: { produto: Produto }) {
    const { isAdmin } = useContext(AuthContext);
    const [enviando, setEnviando] = useState(false);
    const trava = useRef(false);
    const [erro, setErro] = useState('');
    const [pedido, setPedido] = useState<Pedido | null>(null);
    const [opcoes, setOpcoes] = useState<OpcaoCustomizacao[]>([]);
    const [selecionadas, setSelecionadas] = useState<number[]>([]);
    const [textoPersonalizado, setTextoPersonalizado] = useState('');
    const [costas, setCostas] = useState(false);
    const [personalizando, setPersonalizando] = useState(false);
    const [carregando, setCarregando] = useState(false);
    const [erroOpcoes, setErroOpcoes] = useState('');
    const [quantidade, setQuantidade] = useState(1);
    const [revisando, setRevisando] = useState(false);
    const preco = Number(produto.precoBase) + opcoes.filter(o => selecionadas.includes(o.id)).reduce((total, o) => total + Number(o.precoAdicional), 0);
    const opcoesPorTipo = opcoes.reduce<Record<string, OpcaoCustomizacao[]>>((grupos, opcao) => {
        const tipo = tipoDaCustomizacao(opcao.tipo);
        grupos[tipo] = [...(grupos[tipo] ?? []), opcao];
        return grupos;
    }, {});
    const corSelecionada = opcoes.find(opcao => selecionadas.includes(opcao.id) && tipoDaCustomizacao(opcao.tipo) === 'COR');
    const tamanhoSelecionado = opcoes.find(opcao => selecionadas.includes(opcao.id) && tipoDaCustomizacao(opcao.tipo) === 'TAMANHO');
    const estampaSelecionada = opcoes.find(opcao => selecionadas.includes(opcao.id) && tipoDaCustomizacao(opcao.tipo) === 'ESTAMPA_GRAFICA');
    const tintaSelecionada = opcoes.find(opcao => selecionadas.includes(opcao.id) && tipoDaCustomizacao(opcao.tipo) === 'COR_IMPRESSAO');
    const textoHabilitado = opcoes.some(opcao => selecionadas.includes(opcao.id) && tipoDaCustomizacao(opcao.tipo) === 'TEXTO_PERSONALIZADO');
    const simbolo = estampaSelecionada ? simboloDaOpcao(estampaSelecionada.nome) : '';
    const corImpressao = tintaSelecionada ? (/^#[0-9a-f]{6}$/i.test(tintaSelecionada.nome) ? tintaSelecionada.nome : corDaOpcao(tintaSelecionada.nome)) : '#38281F';
    async function personalizar() {
        setPersonalizando(true); setCarregando(true); setErroOpcoes(''); setSelecionadas([]);
        try {
            const disponiveis = await customizacoesService.listarPorProduto(produto.id);
            setOpcoes(disponiveis.filter(opcao => ['COR', 'TAMANHO', 'ESTAMPA_GRAFICA', 'TEXTO_PERSONALIZADO'].includes(tipoDaCustomizacao(opcao.tipo))));
        }
        catch (error) { setOpcoes([]); setErroOpcoes(mensagemApi(error)); }
        finally { setCarregando(false); }
    }
    function selecionar(opcao: OpcaoCustomizacao) {
        setSelecionadas(ids => ids.includes(opcao.id) ? ids.filter(id => id !== opcao.id)
            : [...ids.filter(id => tipoDaCustomizacao(opcoes.find(o => o.id === id)?.tipo ?? '') !== tipoDaCustomizacao(opcao.tipo)), opcao.id]);
    }
    async function pedir() {
        if (trava.current) return;
        trava.current = true; setEnviando(true); setErro('');
        try { setPedido(await pedidosService.criar({ itens: [{ produtoId: produto.id, quantidade, customizacaoIds: selecionadas, textoPersonalizado: textoHabilitado ? textoPersonalizado.trim() : '' }] })); setRevisando(false); }
        catch (error) { setErro(mensagemApi(error)); }
        finally { trava.current = false; setEnviando(false); }
    }
    return <View style={[ui.card, { padding: 0, overflow: 'hidden', flex: 1 }]}>
        <View style={{ backgroundColor: colors.soft, minHeight: 220, alignItems: 'center', justifyContent: 'center', gap: 10, overflow: 'hidden' }}>
            {produto.imagemUrl ? <Image source={{ uri: produto.imagemUrl }} accessibilityLabel={`Foto de ${produto.nome}`} resizeMode="cover" style={{ width: '100%', height: 220 }} /> : <><MaterialCommunityIcons name={produto.categoria === 'ACESSORIO' ? 'hanger' : 'tshirt-crew-outline'} size={100} color={colors.accent} /><Text style={{ fontSize: 10, letterSpacing: 2, color: colors.primary }}>FEITA SOB DEMANDA</Text></>}
        </View>
        <View style={{ padding: 24, gap: 18 }}>
            <View style={{ gap: 8 }}>
                <Text style={ui.eyebrow}>{categorias[produto.categoria]?.toUpperCase()}</Text>
                <Text accessibilityRole="header" style={{ fontFamily: serif, fontSize: 27, color: colors.ink }}>{produto.nome}</Text>
                <Text style={ui.body}>{produto.descricao || 'Uma base para receber a sua identidade.'}</Text>
                <Text style={{ color: colors.primary, fontSize: 22, fontWeight: '600' }}>{money(preco)} <Text style={{ color: colors.muted, fontSize: 12, fontWeight: '400' }}>/ unidade</Text></Text>
            </View>
            {isAdmin ? <>
                <Notice>Administradores configuram produtos e opções de personalização. Pedidos são realizados somente por clientes.</Notice>
                <Button title="Gerenciar produto" icon="settings" onPress={() => router.push('/(tabs)/administracao')} />
            </> : pedido ? <>
                <Notice>Pedido #{pedido.id} recebido. Total confirmado: {money(pedido.valorTotal)}.</Notice>
                <Button title="Acompanhar pedido" icon="arrow-right" onPress={() => router.push('/(tabs)/pedidos')} />
                <Button title="Fazer outro pedido desta peça" secondary onPress={() => { setPedido(null); setErro(''); setQuantidade(1); setSelecionadas([]); setTextoPersonalizado(''); }} />
            </> : revisando ? <>
                <View style={ui.divider} /><Text style={ui.label}>Confira sua escolha</Text>
                <Text style={ui.body}>{quantidade} {quantidade === 1 ? 'unidade' : 'unidades'} · {selecionadas.length ? opcoes.filter(o => selecionadas.includes(o.id)).map(o => `${rotuloTipo(o.tipo)}: ${o.nome}`).join(' · ') : 'Sem customizações'}</Text>
                <Text style={ui.title}>Total estimado: {money(preco * quantidade)}</Text>
                <Text style={[ui.body, { fontSize: 12 }]}>O valor final será confirmado ao enviar o pedido.</Text>
                {textoHabilitado && !!textoPersonalizado.trim() && <Text style={ui.body}>Texto: “{textoPersonalizado.trim()}”</Text>}
                {!!erro && <Notice error>{erro}</Notice>}
                {!!erro && <Button title="Consultar meus pedidos" secondary onPress={() => router.push('/(tabs)/pedidos')} />}
                <Button title="Confirmar pedido" busy={enviando} onPress={() => void pedir()} />
                <Button title="Voltar e ajustar" secondary disabled={enviando} onPress={() => { setRevisando(false); setErro(''); }} />
            </> : <>
                <Button title={personalizando ? 'Remover customizações' : 'Escolher detalhes'} icon="sliders" secondary disabled={carregando} onPress={() => {
                    if (personalizando) { setPersonalizando(false); setSelecionadas([]); setErroOpcoes(''); }
                    else void personalizar();
                }} />
                {personalizando && <View style={{ gap: 18, padding: 16, borderRadius: 16, backgroundColor: colors.background, borderWidth: 1, borderColor: colors.line }}>
                    <View style={{ gap: 4 }}><Text style={ui.eyebrow}>ESTÚDIO POIESIS</Text><Text style={ui.title}>Monte sua peça.</Text><Text style={[ui.body, { fontSize: 13 }]}>Escolha os acabamentos abaixo e veja uma prévia conceitual da sua criação.</Text></View>
                    <View style={{ minHeight: 310, borderRadius: 14, backgroundColor: '#E9E1D6', alignItems: 'center', justifyContent: 'center', overflow: 'hidden' }}>
                        <View style={{ position: 'absolute', width: 210, height: 210, borderRadius: 105, backgroundColor: '#F7F2EB', opacity: .8 }} />
                        <MaterialCommunityIcons name={produto.categoria === 'ACESSORIO' ? 'hanger' : 'tshirt-crew-outline'} size={230} color={corDaOpcao(corSelecionada?.nome ?? 'branco')} style={{ textShadowColor: '#6A56451F', textShadowRadius: 9 }} />
                        {!costas && textoHabilitado && !!textoPersonalizado.trim() && <Text numberOfLines={2} style={{ position: 'absolute', top: 156, color: corImpressao, maxWidth: 120, textAlign: 'center', fontSize: 12, fontWeight: '800', letterSpacing: 1 }}>{textoPersonalizado.trim().toLocaleUpperCase('pt-BR')}</Text>}
                        {!costas && !!simbolo && <Text style={{ position: 'absolute', top: textoPersonalizado.trim() ? 181 : 155, color: corImpressao, fontSize: 27 }}>{simbolo}</Text>}
                        {costas && <Text style={{ position: 'absolute', top: 156, color: corImpressao, fontSize: 12, fontWeight: '800', letterSpacing: 2 }}>POIESIS</Text>}
                        <Pressable accessibilityRole="button" accessibilityLabel={costas ? 'Ver frente da peça' : 'Ver costas da peça'} onPress={() => setCostas(value => !value)} style={{ position: 'absolute', top: 12, right: 12, paddingHorizontal: 13, paddingVertical: 9, borderRadius: 22, backgroundColor: '#FFFCF8', borderWidth: 1, borderColor: colors.line, flexDirection: 'row', alignItems: 'center', gap: 7 }}><Feather name="refresh-cw" size={14} color={colors.primary} /><Text style={{ color: colors.primary, fontSize: 12, fontWeight: '600' }}>{costas ? 'Ver frente' : 'Ver costas'}</Text></Pressable>
                        <Text style={{ position: 'absolute', left: 15, bottom: 12, color: colors.muted, fontSize: 10, letterSpacing: 1.5, fontWeight: '700' }}>{costas ? 'COSTAS' : 'FRENTE'} · {produto.nome.toLocaleUpperCase('pt-BR')}</Text>
                        {!!tamanhoSelecionado && <Text style={{ position: 'absolute', right: 15, bottom: 11, color: colors.primary, fontSize: 11, fontWeight: '800', backgroundColor: '#FFFCF8', paddingHorizontal: 9, paddingVertical: 5, borderRadius: 12 }}>TAM. {tamanhoSelecionado.nome}</Text>}
                    </View>
                    {carregando ? <ActivityIndicator color={colors.primary} /> : erroOpcoes ? <><Notice error>{erroOpcoes}</Notice><Button title="Tentar novamente" secondary onPress={() => void personalizar()} /></> : !opcoes.length ? <Notice error>Este produto ainda não tem opções configuradas pelo ateliê.</Notice> : Object.entries(opcoesPorTipo).map(([tipo, opcoesDoTipo]) => <View key={tipo} style={{ gap: 9 }}>
                        <Text style={ui.label}>{rotuloTipo(tipo)}</Text>
                        <View style={[ui.row, { gap: 8 }]}>{opcoesDoTipo.map(opcao => <Pressable key={opcao.id} accessibilityRole="checkbox" accessibilityLabel={`${opcao.tipo}: ${opcao.nome}`} accessibilityState={{ checked: selecionadas.includes(opcao.id) }} onPress={() => selecionar(opcao)} style={[{ minHeight: 42, paddingHorizontal: 12, paddingVertical: 8, borderRadius: 10, borderWidth: 1, borderColor: colors.line, backgroundColor: colors.paper, flexDirection: 'row', alignItems: 'center', gap: 7 }, selecionadas.includes(opcao.id) && { borderColor: colors.primary, backgroundColor: colors.soft }]}>
                            {['COR', 'COR_IMPRESSAO'].includes(tipo) && <View style={{ width: 16, height: 16, borderRadius: 8, backgroundColor: /^#[0-9a-f]{6}$/i.test(opcao.nome) ? opcao.nome : corDaOpcao(opcao.nome), borderWidth: 1, borderColor: '#00000022' }} />}
                            {tipo === 'ESTAMPA_GRAFICA' && <Text style={{ color: colors.primary, fontSize: 16 }}>{simboloDaOpcao(opcao.nome) || '○'}</Text>}
                            <Text style={{ color: colors.ink, fontSize: 13, fontWeight: selecionadas.includes(opcao.id) ? '700' : '500' }}>{opcao.nome}</Text>
                            {Number(opcao.precoAdicional) > 0 && <Text style={{ color: colors.primary, fontSize: 11 }}>+{money(opcao.precoAdicional)}</Text>}
                        </Pressable>)}</View>
                    </View>)}</View>}
                {textoHabilitado && <Field label="Seu texto" hint="Até 18 caracteres · A prévia atualiza enquanto você digita." placeholder="Ex.: nome, frase ou iniciais" value={textoPersonalizado} onChangeText={setTextoPersonalizado} maxLength={18} />}
                <View style={[ui.row, { justifyContent: 'space-between' }]}>
                    <Text style={ui.label}>Quantidade</Text>
                    <View style={ui.row}>
                        <Pressable accessibilityRole="button" accessibilityLabel={`Diminuir quantidade de ${produto.nome}`} disabled={quantidade === 1} onPress={() => setQuantidade(q => q - 1)} style={[ui.chip, { opacity: quantidade === 1 ? .4 : 1 }]}><Feather name="minus" size={18} color={colors.primary} /></Pressable>
                        <Text accessibilityLiveRegion="polite" style={ui.label}>{quantidade}</Text>
                        <Pressable accessibilityRole="button" accessibilityLabel={`Aumentar quantidade de ${produto.nome}`} onPress={() => setQuantidade(q => q + 1)} style={ui.chip}><Feather name="plus" size={18} color={colors.primary} /></Pressable>
                    </View>
                </View>
                <Button title={`Revisar pedido · ${money(preco * quantidade)}`} icon="arrow-right" disabled={carregando || !!erroOpcoes} onPress={() => setRevisando(true)} />
            </>}
        </View>
    </View>;
}
export default function Catalogo() {
    const { isAdmin } = useContext(AuthContext);
    const { dados: produtos, carregando, erro, carregar } = useConsulta(produtosService.listar, [], 'Não foi possível carregar os produtos.');
    const [busca, setBusca] = useState('');
    const [categoria, setCategoria] = useState('TODAS');
    const { width } = useWindowDimensions();
    const filtrados = produtos.filter(p => (categoria === 'TODAS' || p.categoria === categoria) && `${p.nome} ${p.descricao ?? ''}`.toLocaleLowerCase('pt-BR').includes(busca.toLocaleLowerCase('pt-BR').trim()));
    return <Screen>
        <PageHeader eyebrow={isAdmin ? 'Catálogo do ateliê' : 'CREATE YOUR IDENTITY / STUDIO 01'} title={isAdmin ? 'Produtos disponíveis.' : 'Vista a sua criatividade.'} description={isAdmin ? 'Consulte as peças publicadas e gerencie-as na área de administração.' : 'Uma peça em branco. Infinitas possibilidades. Escolha seu modelo, personalize os detalhes e crie algo exclusivamente seu.'} action={<Button title="Atualizar" icon="refresh-cw" secondary disabled={carregando} onPress={() => void carregar()} />} />
        {!isAdmin && <View style={[ui.row, { gap: 8 }]}>{['01 · Escolher', '02 · Criar', '03 · Pedir'].map((etapa, index) => <View key={etapa} style={[ui.chip, { minHeight: 38, paddingVertical: 8 }, index < 2 && ui.selected]}><Text style={{ color: index < 2 ? colors.primary : colors.muted, fontSize: 12 }}>{etapa}</Text></View>)}</View>}
        <Field label="Encontre sua próxima peça" placeholder="Buscar por nome ou descrição" value={busca} onChangeText={setBusca} />
        <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={{ gap: 10 }}>{['TODAS', ...Object.keys(categorias)].map(c => <Pressable key={c} accessibilityRole="button" accessibilityState={{ selected: categoria === c }} onPress={() => setCategoria(c)} style={[ui.chip, categoria === c && ui.selected]}><Text style={{ color: colors.primary, fontSize: 13 }}>{c === 'TODAS' ? 'Todas as peças' : categorias[c]}</Text></Pressable>)}</ScrollView>
        <View style={[ui.row, { justifyContent: 'space-between' }]}><Text style={ui.title}>O catálogo</Text><Text style={ui.body}>{filtrados.length} {filtrados.length === 1 ? 'peça' : 'peças'}</Text></View>
        {erro ? <EmptyState title="Não conseguimos abrir o catálogo" description={erro} onRetry={() => void carregar()} /> : carregando && !produtos.length ? <EmptyState title="" description="" loading /> : !filtrados.length ? <EmptyState title={produtos.length ? 'Nenhuma peça encontrada' : 'Novas peças em breve'} description={produtos.length ? 'Experimente outro nome ou escolha outra categoria.' : 'As peças disponíveis para personalização aparecerão aqui.'} /> : <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 24, alignItems: 'flex-start' }}>{filtrados.map(produto => <View key={produto.id} style={{ width: width >= 1100 ? '48%' : '100%' }}><CartaoProduto produto={produto} /></View>)}</View>}
    </Screen>;
}
