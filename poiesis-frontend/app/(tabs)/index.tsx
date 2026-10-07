import React, { useCallback, useState } from 'react';
import { ActivityIndicator, FlatList, Pressable, StyleSheet, Text, View } from 'react-native';
import { router, useFocusEffect } from 'expo-router';
import { Feather, MaterialCommunityIcons } from '@expo/vector-icons';
import api from '../../src/services/api';

type Produto = {
    id: number;
    nome: string;
    descricao?: string;
    precoBase: number;
    categoria: string;
};

type Opcao = { id: number; produtoId: number; tipo: string; nome: string; precoAdicional: number; ativo: boolean };

function mensagemErro(error: any) {
    if (error?.response?.status === 401) return 'Sua sessão expirou. Entre novamente.';
    if (error?.response?.status === 403) return 'Sua conta não tem permissão para essa operação.';
    return error?.response?.data?.message ?? 'Não foi possível concluir a solicitação. Verifique se o backend está ativo.';
}

function CartaoProduto({ produto }: { produto: Produto }) {
    const [enviando, setEnviando] = useState(false);
    const [mensagem, setMensagem] = useState('');
    const [criado, setCriado] = useState(false);
    const [opcoes, setOpcoes] = useState<Opcao[]>([]);
    const [selecionadas, setSelecionadas] = useState<number[]>([]);
    const [personalizando, setPersonalizando] = useState(false);
    const [carregandoOpcoes, setCarregandoOpcoes] = useState(false);
    const [erroOpcoes, setErroOpcoes] = useState('');
    const preco = Number(produto.precoBase) + opcoes.filter(o => selecionadas.includes(o.id))
        .reduce((total, o) => total + Number(o.precoAdicional), 0);

    async function personalizar() {
        setPersonalizando(true); setCarregandoOpcoes(true); setErroOpcoes('');
        setSelecionadas([]); setMensagem(''); setCriado(false);
        try {
            const { data } = await api.get<Opcao[]>(`/v1/customizacoes/produto/${produto.id}`);
            setOpcoes(data.filter(o => o.ativo));
        } catch (error) {
            setOpcoes([]); setErroOpcoes(mensagemErro(error));
        } finally {
            setCarregandoOpcoes(false);
        }
    }

    function selecionar(opcao: Opcao) {
        setMensagem(''); setCriado(false);
        setSelecionadas(ids => ids.includes(opcao.id) ? ids.filter(id => id !== opcao.id)
            : [...ids.filter(id => opcoes.find(o => o.id === id)?.tipo.trim().toUpperCase() !== opcao.tipo.trim().toUpperCase()), opcao.id]);
    }
    async function pedir() {
        setEnviando(true);
        setMensagem(''); setCriado(false);
        try {
            await api.post('/v1/pedidos', { itens: [{ produtoId: produto.id, quantidade: 1, customizacaoIds: selecionadas }] });
            setCriado(true);
            setMensagem('Pedido criado. Acompanhe o status na aba Pedidos.');
        } catch (error) {
            setMensagem(mensagemErro(error));
        } finally {
            setEnviando(false);
        }
    }

    return (
        <View style={styles.card}>
            <View style={styles.productRow}>
                <View style={styles.productPreview}>
                    <MaterialCommunityIcons name="tshirt-crew" size={62} color="#718264" />
                </View>
                <View style={styles.productInfo}>
                    <Text style={styles.productIndex}>PRODUTO {String(produto.id).padStart(2, '0')}</Text>
                    <Text style={styles.productName}>{produto.nome}</Text>
                    <Text style={styles.productDescription}>{produto.descricao || produto.categoria}</Text>
                    <Text style={styles.price}>R$ {preco.toFixed(2).replace('.', ',')}</Text>
                </View>
            </View>
            <Pressable accessibilityRole="button" disabled={enviando || carregandoOpcoes} onPress={() => {
                if (personalizando) { setPersonalizando(false); setSelecionadas([]); setErroOpcoes(''); setMensagem(''); setCriado(false); }
                else void personalizar();
            }} style={styles.customButton}>
                <Text style={styles.customLabel}>{personalizando ? 'Voltar à peça sem customização' : 'Escolher customizações'}</Text>
            </Pressable>
            {personalizando && <View style={styles.options}>
                <Text style={styles.optionHint}>Escolha uma opção por tipo. Toque novamente para remover.</Text>
                {carregandoOpcoes ? <ActivityIndicator color="#324B32" /> : erroOpcoes ? <>
                    <Text accessibilityRole="alert" style={styles.optionError}>{erroOpcoes}</Text>
                    <Pressable onPress={() => void personalizar()}><Text style={styles.customLabel}>Tentar novamente</Text></Pressable>
                </> : opcoes.length === 0 ? <Text style={styles.optionHint}>Esta peça ainda não tem opções de customização.</Text> : opcoes.map(opcao => (
                    <Pressable key={opcao.id} accessibilityRole="checkbox" accessibilityState={{ checked: selecionadas.includes(opcao.id), disabled: enviando }}
                        disabled={enviando} onPress={() => selecionar(opcao)} style={[styles.option, selecionadas.includes(opcao.id) && styles.optionSelected]}>
                        <Feather name={selecionadas.includes(opcao.id) ? 'check-square' : 'square'} size={18} color="#324B32" />
                        <Text style={styles.optionName}>{opcao.tipo} · {opcao.nome}</Text>
                        <Text style={styles.customLabel}>+ R$ {Number(opcao.precoAdicional).toFixed(2).replace('.', ',')}</Text>
                    </Pressable>
                ))}
            </View>}
            <Pressable accessibilityRole="button" disabled={enviando || carregandoOpcoes || Boolean(erroOpcoes)} style={[styles.button, (enviando || carregandoOpcoes || Boolean(erroOpcoes)) && styles.buttonDisabled]} onPress={pedir}>
                {enviando ? <ActivityIndicator color="#FFFFFF" /> : <>
                    <Text style={styles.buttonText}>Pedir 1 unidade · R$ {preco.toFixed(2).replace('.', ',')}</Text>
                    <Feather name="arrow-right" size={17} color="#FFFFFF" />
                </>}
            </Pressable>
            {mensagem ? <Text accessibilityRole={criado ? undefined : 'alert'} style={{ marginTop: 12, color: criado ? '#324B32' : '#9B3030' }}>{mensagem}</Text> : null}
            {criado && <Pressable accessibilityRole="button" style={styles.button} onPress={() => router.push('/(tabs)/pedidos')}><Text style={styles.buttonText}>Ver pedidos</Text></Pressable>}
        </View>
    );
}

export default function Catalogo() {
    const [produtos, setProdutos] = useState<Produto[]>([]);
    const [carregando, setCarregando] = useState(true);
    const [erro, setErro] = useState('');

    const carregar = useCallback(async () => {
        setCarregando(true);
        setErro('');
        try {
            const response = await api.get<Produto[]>('/v1/produtos');
            setProdutos(response.data);
        } catch (error) {
            setErro(mensagemErro(error));
        } finally {
            setCarregando(false);
        }
    }, []);

    useFocusEffect(useCallback(() => {
        let ativo = true;
        setCarregando(true);
        setErro('');
        api.get<Produto[]>('/v1/produtos')
            .then(({ data }) => { if (ativo) setProdutos(data); })
            .catch((error) => { if (ativo) { setProdutos([]); setErro(mensagemErro(error)); } })
            .finally(() => { if (ativo) setCarregando(false); });
        return () => { ativo = false; };
    }, []));

    return (
        <FlatList
            style={styles.list}
            contentContainerStyle={styles.content}
            data={produtos}
            keyExtractor={(item) => String(item.id)}
            renderItem={({ item }) => <CartaoProduto produto={item} />}
            ItemSeparatorComponent={() => <View style={styles.separator} />}
            showsVerticalScrollIndicator={false}
            ListHeaderComponent={(
                <View style={styles.header}>
                    <Text style={styles.eyebrow}>POIESIS / CATÁLOGO</Text>
                    <Text style={styles.title}>Sua base.{ '\n' }Sua identidade.</Text>
                    <View style={styles.headerFooter}>
                        <Text style={styles.subtitle}>Produtos disponíveis para pedido.</Text>
                        <Text style={styles.modelCount}>{String(produtos.length).padStart(2, '0')} PRODUTOS</Text>
                    </View>
                </View>
            )}
            ListEmptyComponent={carregando ? <ActivityIndicator style={styles.state} color="#324B32" /> : (
                <View style={styles.emptyState}>
                    <Text style={styles.stateText}>{erro || 'Nenhum produto disponível no catálogo.'}</Text>
                    {erro ? <Pressable onPress={() => void carregar()}><Text style={styles.retry}>Tentar novamente</Text></Pressable> : null}
                </View>
            )}
            ListFooterComponent={<Text style={styles.footer}>PREÇOS E PRODUTOS CARREGADOS DO CATÁLOGO POIESIS</Text>}
        />
    );
}

const styles = StyleSheet.create({
    list: { flex: 1, backgroundColor: '#F3F5F2' },
    content: { width: '100%', maxWidth: 520, alignSelf: 'center', paddingHorizontal: 20, paddingTop: 18, paddingBottom: 34 },
    header: { paddingBottom: 21 },
    eyebrow: { color: '#59635B', fontSize: 10, fontWeight: '800', letterSpacing: 1.1 },
    title: { color: '#202522', fontSize: 34, lineHeight: 37, fontWeight: '900', marginTop: 20 },
    headerFooter: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', gap: 8, marginTop: 10 },
    subtitle: { flex: 1, color: '#667068', fontSize: 12, lineHeight: 18 },
    modelCount: { color: '#89918A', fontSize: 9, fontWeight: '800', letterSpacing: 0.8 },
    card: { padding: 14, borderWidth: 1, borderColor: '#E2E6E1', borderRadius: 7, backgroundColor: '#FFFFFF' },
    productRow: { flexDirection: 'row', alignItems: 'center', gap: 13, marginBottom: 14 },
    productPreview: { width: 82, height: 82, borderRadius: 5, alignItems: 'center', justifyContent: 'center', backgroundColor: '#E9EDE7' },
    productInfo: { flex: 1 },
    productIndex: { color: '#89918A', fontSize: 9, fontWeight: '800', letterSpacing: 0.9 },
    productName: { color: '#202522', fontSize: 15, lineHeight: 19, fontWeight: '800', marginTop: 4 },
    productDescription: { color: '#667068', fontSize: 11, lineHeight: 16, marginTop: 4 },
    price: { color: '#324B32', fontSize: 13, fontWeight: '900', marginTop: 5 },
    button: { minHeight: 44, flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', paddingHorizontal: 13, marginTop: 4, borderRadius: 4, backgroundColor: '#202522' },
    customButton: { minHeight: 44, justifyContent: 'center' },
    customLabel: { color: '#324B32', fontSize: 12, fontWeight: '700' },
    options: { gap: 8, marginBottom: 12 },
    optionHint: { color: '#667068', fontSize: 12, lineHeight: 18 },
    optionError: { color: '#9B3030', fontSize: 12 },
    option: { flexDirection: 'row', alignItems: 'center', gap: 8, minHeight: 44, padding: 8, borderWidth: 1, borderColor: '#E2E6E1', borderRadius: 4 },
    optionSelected: { backgroundColor: '#E5EDE1', borderColor: '#456B43' },
    optionName: { flex: 1, color: '#202522', fontSize: 12 },
    buttonDisabled: { opacity: 0.65 },
    buttonText: { color: '#FFFFFF', fontSize: 12, fontWeight: '800' },
    separator: { height: 12 },
    state: { padding: 35 },
    emptyState: { alignItems: 'center', padding: 24, borderWidth: 1, borderColor: '#E2E6E1', backgroundColor: '#FFFFFF' },
    stateText: { color: '#667068', fontSize: 13, textAlign: 'center' },
    retry: { color: '#324B32', fontWeight: '800', marginTop: 12 },
    footer: { color: '#89918A', fontSize: 9, fontWeight: '700', letterSpacing: 0.6, textAlign: 'center', marginTop: 19, paddingHorizontal: 12 },
});
