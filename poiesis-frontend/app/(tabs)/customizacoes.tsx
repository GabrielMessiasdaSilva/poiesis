import React, { useCallback, useState } from 'react';
import { useFocusEffect } from 'expo-router';
import { ActivityIndicator, FlatList, Pressable, StyleSheet, Text, View } from 'react-native';
import { Feather } from '@expo/vector-icons';
import api from '../../src/services/api';

type Produto = { id: number; nome: string };
type Opcao = { id: number; produtoId: number; tipo: string; nome: string; precoAdicional: number; ativo: boolean };
type OpcoesProduto = { produto: Produto; opcoes: Opcao[] };

export default function Customizacoes() {
    const [dados, setDados] = useState<OpcoesProduto[]>([]);
    const [carregando, setCarregando] = useState(true);
    const [erro, setErro] = useState('');

    const carregar = useCallback(async () => {
        setCarregando(true);
        setErro('');
        try {
            const { data: produtos } = await api.get<Produto[]>('/v1/produtos');
            const resultados = await Promise.all(produtos.map(async (produto) => {
                const { data: opcoes } = await api.get<Opcao[]>(`/v1/customizacoes/produto/${produto.id}`);
                return { produto, opcoes: opcoes.filter((opcao) => opcao.ativo) };
            }));
            setDados(resultados);
        } catch (error: any) {
            setErro(error?.response?.status === 401 ? 'Sua sessão expirou. Entre novamente.' :
                error?.response?.status === 403 ? 'Entre em uma conta para consultar as opções.' :
                    error?.response?.data?.message ?? 'Não foi possível carregar as opções de customização.');
        } finally {
            setCarregando(false);
        }
    }, []);

    useFocusEffect(useCallback(() => {
        let ativo = true;
        setCarregando(true);
        setErro('');
        async function buscarOpcoes() {
            try {
                const { data: produtos } = await api.get<Produto[]>('/v1/produtos');
                const resultados = await Promise.all(produtos.map(async (produto) => {
                    const { data: opcoes } = await api.get<Opcao[]>(`/v1/customizacoes/produto/${produto.id}`);
                    return { produto, opcoes: opcoes.filter((opcao) => opcao.ativo) };
                }));
                if (ativo) setDados(resultados);
            } catch (error: any) {
                if (ativo) setDados([]);
                if (ativo) setErro(error?.response?.status === 401 ? 'Sua sessão expirou. Entre novamente.' :
                    error?.response?.status === 403 ? 'Entre em uma conta para consultar as opções.' :
                        error?.response?.data?.message ?? 'Não foi possível carregar as opções de customização.');
            } finally {
                if (ativo) setCarregando(false);
            }
        }
        void buscarOpcoes();
        return () => { ativo = false; };
    }, []));

    const total = dados.reduce((quantidade, item) => quantidade + item.opcoes.length, 0);

    return (
        <FlatList
            style={styles.list}
            contentContainerStyle={styles.content}
            data={dados.filter((item) => item.opcoes.length > 0)}
            keyExtractor={(item) => String(item.produto.id)}
            showsVerticalScrollIndicator={false}
            renderItem={({ item }) => (
                <View style={styles.card}>
                    <View style={styles.cardHeading}>
                        <View style={styles.icon}><Feather name="edit-3" size={18} color="#456B43" /></View>
                        <View style={styles.titleWrap}>
                            <Text style={styles.itemEyebrow}>PRODUTO {String(item.produto.id).padStart(2, '0')}</Text>
                            <Text style={styles.itemName}>{item.produto.nome}</Text>
                        </View>
                    </View>
                    {item.opcoes.map((opcao) => (
                        <View key={opcao.id} style={styles.optionRow}>
                            <View style={styles.optionCopy}>
                                <Text style={styles.optionType}>{opcao.tipo}</Text>
                                <Text style={styles.optionName}>{opcao.nome}</Text>
                            </View>
                            <Text style={styles.price}>+ R$ {Number(opcao.precoAdicional).toFixed(2).replace('.', ',')}</Text>
                        </View>
                    ))}
                </View>
            )}
            ItemSeparatorComponent={() => <View style={styles.separator} />}
            ListHeaderComponent={(
                <View style={styles.header}>
                    <View style={styles.headerTopline}>
                        <Text style={styles.kicker}>POIESIS / SEU ESPAÇO</Text>
                        <View style={styles.countBadge}><Text style={styles.countText}>{String(total).padStart(2, '0')} OPÇÕES</Text></View>
                    </View>
                    <Text style={styles.title}>Detalhes que fazem{ '\n' }a peça ser sua.</Text>
                    <Text style={styles.subtitle}>Opções ativas cadastradas para cada produto.</Text>
                    <View style={styles.sectionHeading}><Text style={styles.sectionTitle}>Opções do catálogo</Text><Text style={styles.sectionHint}>PERSONALIZAÇÃO</Text></View>
                </View>
            )}
            ListEmptyComponent={carregando ? <ActivityIndicator style={styles.state} color="#324B32" /> : (
                <View style={styles.emptyState}>
                    <Feather name={erro ? 'alert-circle' : 'edit-3'} size={30} color="#89918A" />
                    <Text style={styles.emptyTitle}>{erro ? 'Opções indisponíveis' : 'Nenhuma opção cadastrada'}</Text>
                    <Text style={styles.emptyText}>{erro || 'Quando houver opções ativas no backend, elas aparecerão aqui.'}</Text>
                    {erro ? <Pressable onPress={() => void carregar()}><Text style={styles.retry}>Tentar novamente</Text></Pressable> : null}
                </View>
            )}
            ListFooterComponent={<Text style={styles.footerNote}>OPÇÕES LIDAS DO SERVIÇO DE CUSTOMIZAÇÃO</Text>}
        />
    );
}

const styles = StyleSheet.create({
    list: { flex: 1, backgroundColor: '#F3F5F2' },
    content: { width: '100%', maxWidth: 520, alignSelf: 'center', paddingHorizontal: 20, paddingTop: 18, paddingBottom: 34 },
    header: { paddingBottom: 22 },
    headerTopline: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: 24 },
    kicker: { color: '#59635B', fontSize: 11, fontWeight: '800', letterSpacing: 1.1 },
    countBadge: { backgroundColor: '#DDE8D8', paddingHorizontal: 11, paddingVertical: 7, borderRadius: 4 },
    countText: { color: '#324B32', fontSize: 10, fontWeight: '800', letterSpacing: 0.7 },
    title: { color: '#202522', fontSize: 32, lineHeight: 37, fontWeight: '900' },
    subtitle: { color: '#667068', fontSize: 14, lineHeight: 21, marginTop: 11, maxWidth: 330 },
    sectionHeading: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginTop: 28 },
    sectionTitle: { color: '#202522', fontSize: 18, fontWeight: '800' },
    sectionHint: { color: '#89918A', fontSize: 10, fontWeight: '800', letterSpacing: 1 },
    card: { padding: 15, borderRadius: 7, backgroundColor: '#FFFFFF', borderWidth: 1, borderColor: '#E2E6E1' },
    cardHeading: { flexDirection: 'row', alignItems: 'center', gap: 12, paddingBottom: 13 },
    icon: { width: 42, height: 42, alignItems: 'center', justifyContent: 'center', backgroundColor: '#E9EDE7', borderRadius: 4 },
    titleWrap: { flex: 1 },
    itemEyebrow: { color: '#89918A', fontSize: 9, fontWeight: '800', letterSpacing: 1 },
    itemName: { color: '#202522', fontSize: 16, fontWeight: '800', marginTop: 4 },
    optionRow: { minHeight: 55, flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', borderTopWidth: 1, borderTopColor: '#EEF0ED', gap: 10 },
    optionCopy: { flex: 1 },
    optionType: { color: '#89918A', fontSize: 8, fontWeight: '800', letterSpacing: 0.8 },
    optionName: { color: '#465149', fontSize: 12, fontWeight: '700', marginTop: 3 },
    price: { color: '#324B32', fontSize: 11, fontWeight: '800' },
    separator: { height: 14 },
    state: { padding: 35 },
    emptyState: { alignItems: 'center', paddingHorizontal: 25, paddingVertical: 40, borderWidth: 1, borderColor: '#E2E6E1', borderRadius: 6, backgroundColor: '#FFFFFF' },
    emptyTitle: { color: '#202522', fontSize: 15, fontWeight: '800', marginTop: 12 },
    emptyText: { color: '#667068', fontSize: 12, lineHeight: 18, textAlign: 'center', marginTop: 6 },
    retry: { color: '#324B32', fontWeight: '800', marginTop: 12 },
    footerNote: { color: '#89918A', fontSize: 9, lineHeight: 14, fontWeight: '700', letterSpacing: 0.6, textAlign: 'center', marginTop: 23, paddingHorizontal: 15 },
});
