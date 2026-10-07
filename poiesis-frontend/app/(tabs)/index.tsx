import React, { useCallback, useEffect, useState } from 'react';
import { ActivityIndicator, Alert, FlatList, Pressable, StyleSheet, Text, View } from 'react-native';
import { router } from 'expo-router';
import { Feather, MaterialCommunityIcons } from '@expo/vector-icons';
import api from '../../src/services/api';

type Produto = {
    id: number;
    nome: string;
    descricao?: string;
    precoBase: number;
    categoria: string;
};

function mensagemErro(error: any) {
    if (error?.response?.status === 401) return 'Sua sessão expirou. Entre novamente.';
    if (error?.response?.status === 403) return 'Sua conta não tem permissão para essa operação.';
    return error?.response?.data?.message ?? 'Não foi possível concluir a solicitação. Verifique se o backend está ativo.';
}

function CartaoProduto({ produto }: { produto: Produto }) {
    const [enviando, setEnviando] = useState(false);
    async function pedir() {
        setEnviando(true);
        try {
            await api.post('/v1/pedidos', { itens: [{ produtoId: produto.id, quantidade: 1 }] });
            Alert.alert('Pedido criado', 'Seu pedido foi registrado. Você pode acompanhar o status na aba Pedidos.', [
                { text: 'Ver pedidos', onPress: () => router.push('/(tabs)/pedidos') },
                { text: 'Continuar no catálogo' },
            ]);
        } catch (error) {
            Alert.alert('Não foi possível criar o pedido', mensagemErro(error));
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
                    <Text style={styles.price}>R$ {Number(produto.precoBase).toFixed(2).replace('.', ',')}</Text>
                </View>
            </View>
            <Pressable accessibilityRole="button" disabled={enviando} style={[styles.button, enviando && styles.buttonDisabled]} onPress={pedir}>
                {enviando ? <ActivityIndicator color="#FFFFFF" /> : <>
                    <Text style={styles.buttonText}>Pedir esta peça · 1 unidade</Text>
                    <Feather name="arrow-right" size={17} color="#FFFFFF" />
                </>}
            </Pressable>
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

    useEffect(() => {
        let ativo = true;
        api.get<Produto[]>('/v1/produtos')
            .then(({ data }) => { if (ativo) setProdutos(data); })
            .catch((error) => { if (ativo) setErro(mensagemErro(error)); })
            .finally(() => { if (ativo) setCarregando(false); });
        return () => { ativo = false; };
    }, []);

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
    buttonDisabled: { opacity: 0.65 },
    buttonText: { color: '#FFFFFF', fontSize: 12, fontWeight: '800' },
    separator: { height: 12 },
    state: { padding: 35 },
    emptyState: { alignItems: 'center', padding: 24, borderWidth: 1, borderColor: '#E2E6E1', backgroundColor: '#FFFFFF' },
    stateText: { color: '#667068', fontSize: 13, textAlign: 'center' },
    retry: { color: '#324B32', fontWeight: '800', marginTop: 12 },
    footer: { color: '#89918A', fontSize: 9, fontWeight: '700', letterSpacing: 0.6, textAlign: 'center', marginTop: 19, paddingHorizontal: 12 },
});
