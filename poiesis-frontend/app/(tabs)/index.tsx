import { useContext, useRef, useState } from 'react';
import { ActivityIndicator, Image, Pressable, ScrollView, Text, View, useWindowDimensions } from 'react-native';
import { router } from 'expo-router';
import { Feather, MaterialCommunityIcons } from '@expo/vector-icons';
import { produtosService, customizacoesService, type Produto, type OpcaoCustomizacao } from '../../src/services/produtos';
import { pedidosService, type Pedido } from '../../src/services/pedidos';
import { mensagemApi } from '../../src/services/erros';
import { AuthContext } from '../../src/contexts/AuthContext';
import { artesBase, referenciaArteBase } from '../../src/data/assetsPersonalizacao';
import { useConsulta } from '../../src/hooks/useConsulta';
import { Button, EmptyState, Field, Notice, PageHeader, Screen } from '../../src/ui/components';
import { colors, money, serif, ui } from '../../src/ui/theme';

const categorias: Record<string, string> = { CAMISA: 'Camisas', CAMISETA: 'Camisetas', MOLETOM: 'Moletons', CALCA: 'Calças', ACESSORIO: 'Acessórios' };
const corDaOpcao = (nome: string) => ({
    branco: '#FBF8F2', preto: '#151515', azul: '#1F5FAF', vermelho: '#C9362B', verde: '#36724D', amarelo: '#E7B82C', rosa: '#D95A91', laranja: '#E78131', cinza: '#8B8D90', marrom: '#704A35', bege: '#D8C5A8', roxo: '#6C4B8E', azulmarinho: '#132C67', vinho: '#702331', turquesa: '#26AFC4', offwhite: '#EEE9DF',
}[nome.toLocaleLowerCase('pt-BR').replace(/[\s-]/g, '')] ?? colors.paper);
const tipoNormalizado = (tipo: string) => tipo.trim().toLocaleUpperCase('pt-BR').replace(/[\s-]/g, '_');
function CartaoProduto({ produto }: { produto: Produto }) {
    const { isAdmin } = useContext(AuthContext);
    const [enviando, setEnviando] = useState(false);
    const trava = useRef(false);
    const [erro, setErro] = useState('');
    const [pedido, setPedido] = useState<Pedido | null>(null);
    const [opcoes, setOpcoes] = useState<OpcaoCustomizacao[]>([]);
    const [selecionadas, setSelecionadas] = useState<number[]>([]);
    const [textoPersonalizado, setTextoPersonalizado] = useState('');
    const [referenciaImagem, setReferenciaImagem] = useState('');
    const [personalizando, setPersonalizando] = useState(false);
    const [carregando, setCarregando] = useState(false);
    const [erroOpcoes, setErroOpcoes] = useState('');
    const [quantidade, setQuantidade] = useState(1);
    const [revisando, setRevisando] = useState(false);
    const preco = Number(produto.precoBase) + opcoes.filter(o => selecionadas.includes(o.id)).reduce((total, o) => total + Number(o.precoAdicional), 0);
    const opcoesPorTipo = opcoes.reduce<Record<string, OpcaoCustomizacao[]>>((grupos, opcao) => {
        const tipo = tipoNormalizado(opcao.tipo);
        grupos[tipo] = [...(grupos[tipo] ?? []), opcao];
        return grupos;
    }, {});
    const corSelecionada = opcoes.find(opcao => selecionadas.includes(opcao.id) && ['COR', 'COLOR'].includes(tipoNormalizado(opcao.tipo)));
    async function personalizar() {
        setPersonalizando(true); setCarregando(true); setErroOpcoes(''); setSelecionadas([]);
        try { setOpcoes(await customizacoesService.listarPorProduto(produto.id)); }
        catch (error) { setOpcoes([]); setErroOpcoes(mensagemApi(error)); }
        finally { setCarregando(false); }
    }
    function selecionar(opcao: OpcaoCustomizacao) {
        setSelecionadas(ids => ids.includes(opcao.id) ? ids.filter(id => id !== opcao.id)
            : [...ids.filter(id => opcoes.find(o => o.id === id)?.tipo.trim().toUpperCase() !== opcao.tipo.trim().toUpperCase()), opcao.id]);
    }
    async function pedir() {
        if (trava.current) return;
        const arte = referenciaImagem.trim();
        if (arte && !/^(https?:\/\/\S+|asset:\/\/poiesis\/[a-z0-9-]+)$/i.test(arte)) { setErro('Escolha uma arte da biblioteca ou informe uma URL HTTP/HTTPS.'); setRevisando(true); return; }
        trava.current = true; setEnviando(true); setErro('');
        try { setPedido(await pedidosService.criar({ itens: [{ produtoId: produto.id, quantidade, customizacaoIds: selecionadas, textoPersonalizado: textoPersonalizado.trim(), referenciaImagem: arte }] })); setRevisando(false); }
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
                <Button title="Fazer outro pedido desta peça" secondary onPress={() => { setPedido(null); setErro(''); setQuantidade(1); setSelecionadas([]); setTextoPersonalizado(''); setReferenciaImagem(''); }} />
            </> : revisando ? <>
                <View style={ui.divider} /><Text style={ui.label}>Confira sua escolha</Text>
                <Text style={ui.body}>{quantidade} {quantidade === 1 ? 'unidade' : 'unidades'} · {selecionadas.length ? opcoes.filter(o => selecionadas.includes(o.id)).map(o => `${o.tipo}: ${o.nome}`).join(' · ') : 'Sem customizações'}</Text>
                <Text style={ui.title}>Total estimado: {money(preco * quantidade)}</Text>
                <Text style={[ui.body, { fontSize: 12 }]}>O valor final será confirmado ao enviar o pedido.</Text>
                {!!textoPersonalizado.trim() && <Text style={ui.body}>Texto: “{textoPersonalizado.trim()}”</Text>}
                {!!referenciaImagem.trim() && <Text style={ui.body}>Arte: {referenciaImagem.startsWith('asset://') ? 'Biblioteca Poiesis' : referenciaImagem.trim()}</Text>}
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
                    <View style={{ minHeight: 235, borderRadius: 14, backgroundColor: '#E9E1D6', alignItems: 'center', justifyContent: 'center', overflow: 'hidden' }}>
                        <View style={{ position: 'absolute', width: 150, height: 150, borderRadius: 75, backgroundColor: '#F7F2EB', opacity: .8 }} />
                        <MaterialCommunityIcons name={produto.categoria === 'MOLETOM' ? 'hoodie' : produto.categoria === 'ACESSORIO' ? 'hanger' : 'tshirt-crew-outline'} size={180} color={corDaOpcao(corSelecionada?.nome ?? 'branco')} style={{ textShadowColor: '#6A56451F', textShadowRadius: 9 }} />
                        {!!referenciaImagem && <View style={{ position: 'absolute', alignItems: 'center', justifyContent: 'center', top: 94 }}><Feather name={referenciaImagem.startsWith('asset://') ? 'image' : 'link'} size={28} color={colors.primary} /></View>}
                        {!!textoPersonalizado.trim() && <Text style={{ position: 'absolute', top: 133, color: corSelecionada?.nome.toLowerCase() === 'preto' ? colors.paper : colors.ink, maxWidth: 105, textAlign: 'center', fontSize: 11, fontWeight: '700' }}>{textoPersonalizado.trim()}</Text>}
                    </View>
                    {carregando ? <ActivityIndicator color={colors.primary} /> : erroOpcoes ? <><Notice error>{erroOpcoes}</Notice><Button title="Tentar novamente" secondary onPress={() => void personalizar()} /></> : !opcoes.length ? <Notice error>Esta peça ainda não tem opções cadastradas. Cadastre cor, tamanho e técnicas no painel administrativo.</Notice> : Object.entries(opcoesPorTipo).map(([tipo, opcoesDoTipo]) => <View key={tipo} style={{ gap: 9 }}>
                        <Text style={ui.label}>{tipo.replaceAll('_', ' ')}</Text>
                        <View style={[ui.row, { gap: 8 }]}>{opcoesDoTipo.map(opcao => <Pressable key={opcao.id} accessibilityRole="checkbox" accessibilityLabel={`${opcao.tipo}: ${opcao.nome}`} accessibilityState={{ checked: selecionadas.includes(opcao.id) }} onPress={() => selecionar(opcao)} style={[{ minHeight: 42, paddingHorizontal: 12, paddingVertical: 8, borderRadius: 10, borderWidth: 1, borderColor: colors.line, backgroundColor: colors.paper, flexDirection: 'row', alignItems: 'center', gap: 7 }, selecionadas.includes(opcao.id) && { borderColor: colors.primary, backgroundColor: colors.soft }]}>
                            {['COR', 'COLOR'].includes(tipo) && <View style={{ width: 16, height: 16, borderRadius: 8, backgroundColor: corDaOpcao(opcao.nome), borderWidth: 1, borderColor: '#00000022' }} />}
                            <Text style={{ color: colors.ink, fontSize: 13, fontWeight: selecionadas.includes(opcao.id) ? '700' : '500' }}>{opcao.nome}</Text>
                            {Number(opcao.precoAdicional) > 0 && <Text style={{ color: colors.primary, fontSize: 11 }}>+{money(opcao.precoAdicional)}</Text>}
                        </Pressable>)}</View>
                    </View>)}</View>}
                <View style={{ gap: 10 }}>
                    <Text style={ui.label}>Arte para a peça</Text>
                    <Text style={[ui.body, { fontSize: 13 }]}>Escolha uma arte-base ou informe o link da sua própria arte.</Text>
                    <View style={[ui.row, { alignItems: 'stretch' }]}>{artesBase.map(arte => <Pressable key={arte.id} accessibilityRole="button" accessibilityState={{ selected: referenciaImagem === referenciaArteBase(arte.id) }} onPress={() => setReferenciaImagem(referenciaImagem === referenciaArteBase(arte.id) ? '' : referenciaArteBase(arte.id))} style={[ui.chip, { width: 150, gap: 5 }, referenciaImagem === referenciaArteBase(arte.id) && ui.selected]}><Feather name={arte.id === 'flor-geometrica' ? 'aperture' : 'sun'} size={22} color={colors.accent} /><Text style={{ color: colors.ink, fontWeight: '600', fontSize: 12 }}>{arte.nome}</Text><Text style={{ color: colors.muted, fontSize: 10 }}>{arte.descricao}</Text></Pressable>)}</View>
                    <Field label="Texto para personalizar (opcional)" placeholder="Ex.: nome, frase ou iniciais" value={textoPersonalizado} onChangeText={setTextoPersonalizado} maxLength={300} />
                    <Field label="URL da imagem/arte (opcional)" hint="Uma imagem pública em HTTP ou HTTPS." placeholder="https://…" value={referenciaImagem.startsWith('asset://') ? '' : referenciaImagem} onChangeText={setReferenciaImagem} autoCapitalize="none" keyboardType="url" maxLength={2048} />
                </View>
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
        <PageHeader eyebrow={isAdmin ? 'Catálogo do ateliê' : 'O começo da sua peça'} title={isAdmin ? 'Produtos disponíveis.' : 'Vista a sua essência.'} description={isAdmin ? 'Consulte as peças publicadas e gerencie-as na área de administração.' : 'Escolha uma base, acrescente seus detalhes e crie uma peça com a sua identidade. Produção sob demanda, feita a partir do seu pedido.'} action={<Button title="Atualizar" icon="refresh-cw" secondary disabled={carregando} onPress={() => void carregar()} />} />
        <Field label="Encontre sua próxima peça" placeholder="Buscar por nome ou descrição" value={busca} onChangeText={setBusca} />
        <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={{ gap: 10 }}>{['TODAS', ...Object.keys(categorias)].map(c => <Pressable key={c} accessibilityRole="button" accessibilityState={{ selected: categoria === c }} onPress={() => setCategoria(c)} style={[ui.chip, categoria === c && ui.selected]}><Text style={{ color: colors.primary, fontSize: 13 }}>{c === 'TODAS' ? 'Todas as peças' : categorias[c]}</Text></Pressable>)}</ScrollView>
        <View style={[ui.row, { justifyContent: 'space-between' }]}><Text style={ui.title}>O catálogo</Text><Text style={ui.body}>{filtrados.length} {filtrados.length === 1 ? 'peça' : 'peças'}</Text></View>
        {erro ? <EmptyState title="Não conseguimos abrir o catálogo" description={erro} onRetry={() => void carregar()} /> : carregando && !produtos.length ? <EmptyState title="" description="" loading /> : !filtrados.length ? <EmptyState title={produtos.length ? 'Nenhuma peça encontrada' : 'Novas peças em breve'} description={produtos.length ? 'Experimente outro nome ou escolha outra categoria.' : 'As peças disponíveis para personalização aparecerão aqui.'} /> : <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 24, alignItems: 'flex-start' }}>{filtrados.map(produto => <View key={produto.id} style={{ width: width >= 1100 ? '48%' : '100%' }}><CartaoProduto produto={produto} /></View>)}</View>}
    </Screen>;
}
