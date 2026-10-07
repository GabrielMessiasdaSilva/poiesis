import React, { useCallback, useEffect, useState } from 'react';
import { ActivityIndicator, FlatList, Pressable, StyleSheet, Text, View } from 'react-native';
import { Feather } from '@expo/vector-icons';
import api from '../../src/services/api';

type Pedido = {
    id: number;
    itens: { produtoId: number; nomeProduto: string; quantidade: number; precoUnitario: number; subtotal: number }[];
    valorTotal: number;
    status: 'CRIADO' | 'EM_PROCESSAMENTO' | 'FINALIZADO' | 'CANCELADO';
    dataCriacao: string;
};

const statusLabel: Record<Pedido['status'], string> = {
    CRIADO: 'Pedido recebido', EM_PROCESSAMENTO: 'Em processamento', FINALIZADO: 'Finalizado', CANCELADO: 'Cancelado',
};

function formatarData(value: string) {
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? value : date.toLocaleDateString('pt-BR');
}

export default function Pedidos() {
    const [pedidos, setPedidos] = useState<Pedido[]>([]);
    const [carregando, setCarregando] = useState(true);
    const [erro, setErro] = useState('');

    const carregar = useCallback(async () => {
        setCarregando(true);
        setErro('');
        try {
            const response = await api.get<Pedido[]>('/v1/pedidos');
            setPedidos(response.data);
        } catch (error: any) {
            setErro(error?.response?.status === 401
                ? 'Sua sessão expirou. Entre novamente.'
                : error?.response?.data?.message ?? 'Não foi possível carregar os pedidos. Verifique se o backend está ativo.');
        } finally {
            setCarregando(false);
        }
    }, []);

    useEffect(() => {
        let ativo = true;
        api.get<Pedido[]>('/v1/pedidos')
            .then(({ data }) => { if (ativo) setPedidos(data); })
            .catch((error: any) => {
                if (ativo) setErro(error?.response?.status === 401
                    ? 'Sua sessão expirou. Entre novamente.'
                    : error?.response?.data?.message ?? 'Não foi possível carregar os pedidos. Verifique se o backend está ativo.');
            })
            .finally(() => { if (ativo) setCarregando(false); });
        return () => { ativo = false; };
    }, []);

    const emAndamento = pedidos.filter((pedido) => pedido.status === 'CRIADO' || pedido.status === 'EM_PROCESSAMENTO').length;
    const finalizados = pedidos.filter((pedido) => pedido.status === 'FINALIZADO').length;

    return (
        <FlatList
            style={styles.list}
            contentContainerStyle={styles.content}
            data={pedidos}
            keyExtractor={(pedido) => String(pedido.id)}
            renderItem={({ item }) => (
                <View style={styles.card}>
                    <View style={styles.cardTopline}>
                        <View>
                            <Text style={styles.orderEyebrow}>PEDIDO Nº {item.id}</Text>
                            <Text style={styles.orderDate}>{formatarData(item.dataCriacao)}</Text>
                        </View>
                        <View style={styles.statusBadge}>
                            <View style={styles.statusDot} />
                            <Text style={styles.statusText}>{statusLabel[item.status] ?? item.status}</Text>
                        </View>
                    </View>
                    {item.itens.map((linha, index) => (
                        <View key={`${linha.produtoId}-${index}`} style={styles.itemRow}>
                            <Feather name="package" size={17} color="#718264" />
                            <Text style={styles.productName}>{linha.nomeProduto}</Text>
                            <Text style={styles.quantity}>× {linha.quantidade}</Text>
                        </View>
                    ))}
                    <View style={styles.totalRow}>
                        <Text style={styles.totalLabel}>TOTAL DO PEDIDO</Text>
                        <Text style={styles.totalValue}>R$ {Number(item.valorTotal).toFixed(2).replace('.', ',')}</Text>
                    </View>
                </View>
            )}
            ItemSeparatorComponent={() => <View style={styles.separator} />}
            showsVerticalScrollIndicator={false}
            ListHeaderComponent={(
                <View style={styles.header}>
                    <View style={styles.headerTopline}>
                        <Text style={styles.eyebrow}>POIESIS / ACOMPANHAMENTO</Text>
                        <View style={styles.countBadge}><Text style={styles.countText}>{String(pedidos.length).padStart(2, '0')} PEDIDOS</Text></View>
                    </View>
                    <Text style={styles.title}>Acompanhe seus pedidos.</Text>
                    <Text style={styles.subtitle}>Status e valores atualizados pelo backend.</Text>
                    <View style={styles.summaryRow}>
                        <View style={styles.summaryItem}><Text style={styles.summaryNumber}>{String(emAndamento).padStart(2, '0')}</Text><Text style={styles.summaryLabel}>EM ANDAMENTO</Text></View>
                        <View style={styles.summaryDivider} />
                        <View style={styles.summaryItem}><Text style={styles.summaryNumber}>{String(finalizados).padStart(2, '0')}</Text><Text style={styles.summaryLabel}>FINALIZADOS</Text></View>
                        <Feather name="activity" size={19} color="#718264" />
                    </View>
                    <Text style={styles.sectionTitle}>Seus pedidos</Text>
                </View>
            )}
            ListEmptyComponent={carregando ? <ActivityIndicator style={styles.state} color="#324B32" /> : (
                <View style={styles.emptyState}>
                    <Text style={styles.emptyTitle}>{erro ? 'Pedidos indisponíveis' : 'Nenhum pedido por aqui'}</Text>
                    <Text style={styles.emptyText}>{erro || 'Quando fizer um pedido, o acompanhamento aparece nesta tela.'}</Text>
                    {erro ? <Pressable onPress={() => void carregar()}><Text style={styles.retry}>Tentar novamente</Text></Pressable> : null}
                </View>
            )}
        />
    );
}

const styles = StyleSheet.create({
    list: { flex: 1, backgroundColor: '#F3F5F2' },
    content: { width: '100%', maxWidth: 520, alignSelf: 'center', paddingHorizontal: 20, paddingTop: 18, paddingBottom: 34 },
    header: { paddingBottom: 20 },
    headerTopline: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' },
    eyebrow: { color: '#59635B', fontSize: 10, fontWeight: '800', letterSpacing: 1 },
    countBadge: { backgroundColor: '#DDE8D8', paddingHorizontal: 10, paddingVertical: 7, borderRadius: 4 },
    countText: { color: '#324B32', fontSize: 9, fontWeight: '800', letterSpacing: 0.7 },
    title: { color: '#202522', fontSize: 30, lineHeight: 35, fontWeight: '900', marginTop: 22, maxWidth: 360 },
    subtitle: { color: '#667068', fontSize: 13, lineHeight: 19, marginTop: 7 },
    summaryRow: { minHeight: 70, flexDirection: 'row', alignItems: 'center', gap: 16, paddingHorizontal: 15, marginTop: 21, backgroundColor: '#E9EDE7', borderRadius: 5 },
    summaryItem: { minWidth: 82 },
    summaryNumber: { color: '#202522', fontSize: 19, lineHeight: 23, fontWeight: '900' },
    summaryLabel: { color: '#667068', fontSize: 8, fontWeight: '800', letterSpacing: 0.6, marginTop: 2 },
    summaryDivider: { width: 1, height: 31, backgroundColor: '#CDD5CB' },
    sectionTitle: { color: '#202522', fontSize: 17, fontWeight: '800', marginTop: 25 },
    card: { padding: 15, borderWidth: 1, borderColor: '#E2E6E1', borderRadius: 7, backgroundColor: '#FFFFFF' },
    cardTopline: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', gap: 8 },
    orderEyebrow: { color: '#202522', fontSize: 11, fontWeight: '900', letterSpacing: 0.6 },
    orderDate: { color: '#89918A', fontSize: 9, fontWeight: '700', marginTop: 4 },
    statusBadge: { minHeight: 27, flexDirection: 'row', alignItems: 'center', gap: 6, paddingHorizontal: 9, borderRadius: 4, backgroundColor: '#E5EDE1' },
    statusDot: { width: 6, height: 6, borderRadius: 3, backgroundColor: '#456B43' },
    statusText: { color: '#456B43', fontSize: 9, fontWeight: '800' },
    itemRow: { minHeight: 48, flexDirection: 'row', alignItems: 'center', gap: 10, marginTop: 12, borderTopWidth: 1, borderTopColor: '#EEF0ED' },
    productName: { flex: 1, color: '#202522', fontSize: 13, fontWeight: '700' },
    quantity: { color: '#667068', fontSize: 12, fontWeight: '700' },
    totalRow: { flexDirection: 'row', justifyContent: 'space-between', borderTopWidth: 1, borderTopColor: '#EEF0ED', paddingTop: 12, marginTop: 2 },
    totalLabel: { color: '#89918A', fontSize: 9, fontWeight: '800', letterSpacing: 0.6 },
    totalValue: { color: '#202522', fontSize: 13, fontWeight: '900' },
    separator: { height: 12 },
    state: { padding: 35 },
    emptyState: { alignItems: 'center', paddingHorizontal: 25, paddingVertical: 40, borderWidth: 1, borderColor: '#E2E6E1', borderRadius: 6, backgroundColor: '#FFFFFF' },
    emptyTitle: { color: '#202522', fontSize: 15, fontWeight: '800' },
    emptyText: { color: '#667068', fontSize: 12, lineHeight: 18, textAlign: 'center', marginTop: 6 },
    retry: { color: '#324B32', fontWeight: '800', marginTop: 12 },
});
