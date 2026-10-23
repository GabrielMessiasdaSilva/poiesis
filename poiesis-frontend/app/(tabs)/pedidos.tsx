import { useCallback, useContext, useEffect, useState } from 'react';
import { Pressable, Text, View } from 'react-native';
import { router, useFocusEffect } from 'expo-router';
import { Feather } from '@expo/vector-icons';
import { pedidosService, type Pedido } from '../../src/services/pedidos';
import { producaoService, type OrdemProducao, type StatusProducao } from '../../src/services/producao';
import { useConsulta } from '../../src/hooks/useConsulta';
import { AuthContext } from '../../src/contexts/AuthContext';
import { Button, EmptyState, PageHeader, Screen } from '../../src/ui/components';
import { colors, money, ui } from '../../src/ui/theme';
const labels: Record<Pedido['status'], string> = { CRIADO: 'Recebido', EM_PROCESSAMENTO: 'Em processamento', FINALIZADO: 'Finalizado', CANCELADO: 'Cancelado' };
const etapas: StatusProducao[] = ['PENDENTE', 'APROVADO', 'EM_CORTE', 'EM_COSTURA', 'EM_PERSONALIZACAO', 'FINALIZADO', 'ENTREGUE'];
const labelsProducao: Record<StatusProducao, string> = { PENDENTE: 'Pendente', APROVADO: 'Aprovado', EM_CORTE: 'Em corte', EM_COSTURA: 'Em costura', EM_PERSONALIZACAO: 'Em personalização', FINALIZADO: 'Finalizado', ENTREGUE: 'Entregue', CANCELADO: 'Cancelado' };
function data(value: string) { const date = new Date(value); return Number.isNaN(date.getTime()) ? value : date.toLocaleDateString('pt-BR', { day: '2-digit', month: 'long', year: 'numeric' }); }
export default function Pedidos() {
    const { isAdmin } = useContext(AuthContext);
    const { dados: pedidos, carregando, erro, carregar } = useConsulta(pedidosService.listar, [], 'Não foi possível carregar seus pedidos.');
    const [filtro, setFiltro] = useState('Todos');
    const [producoes, setProducoes] = useState<Record<number, OrdemProducao>>({});
    const consultarProducao = useCallback(async () => {
        if (isAdmin || !pedidos.length) return;
        const respostas = await Promise.all(pedidos.map(async pedido => {
            try { return [pedido.id, await producaoService.consultarPedido(pedido.id)] as const; }
            catch { return null; }
        }));
        setProducoes(Object.fromEntries(respostas.filter((item): item is readonly [number, OrdemProducao] => item !== null)));
    }, [isAdmin, pedidos]);
    useEffect(() => {
        const timer = setTimeout(() => { void consultarProducao(); }, 0);
        return () => clearTimeout(timer);
    }, [consultarProducao]);
    useFocusEffect(useCallback(() => {
        const timer = setInterval(() => {
            void carregar();
            void consultarProducao();
        }, 15000);
        return () => clearInterval(timer);
    }, [carregar, consultarProducao]));
    const exibidos = pedidos.filter(p => {
        const status = producoes[p.id]?.status;
        return filtro === 'Todos' || (filtro === 'Em andamento' ? status !== 'ENTREGUE' && status !== 'CANCELADO' : status === 'ENTREGUE' || p.status === 'FINALIZADO');
    }).sort((a, b) => b.id - a.id);
    return <Screen narrow>
        <PageHeader eyebrow="Cada escolha, uma história" title={isAdmin ? 'Pedidos do ateliê.' : 'Seus pedidos.'} description={isAdmin ? 'Consulte os pedidos recebidos e os detalhes de cada cliente.' : 'Suas peças, escolhas e valores reunidos em um só lugar.'} action={<Button title="Atualizar" icon="refresh-cw" secondary disabled={carregando} onPress={() => { void carregar(); void consultarProducao(); }} />} />
        <View style={ui.row}>{['Todos', 'Em andamento', 'Finalizados'].map(item => <Pressable key={item} accessibilityRole="button" accessibilityState={{ selected: filtro === item }} onPress={() => setFiltro(item)} style={[ui.chip, filtro === item && ui.selected]}><Text style={{ color: colors.primary }}>{item}</Text></Pressable>)}</View>
        {erro ? <EmptyState title="Pedidos indisponíveis" description={erro} onRetry={() => void carregar()} /> : carregando && !pedidos.length ? <EmptyState title="" description="" loading /> : !exibidos.length ? <><EmptyState title={pedidos.length ? 'Nenhum pedido neste filtro' : 'Sua primeira peça começa aqui'} description={pedidos.length ? 'Escolha outro filtro para consultar os pedidos.' : 'Explore o catálogo e escolha os detalhes do seu primeiro pedido.'} /><Button title="Explorar catálogo" onPress={() => router.push('/(tabs)')} /></> : exibidos.map(pedido => <View key={pedido.id} style={ui.card}>
            <View style={[ui.row, { justifyContent: 'space-between' }]}>
                <View style={{ gap: 6, flexShrink: 1 }}><Text style={ui.title}>Pedido #{pedido.id}</Text><Text style={[ui.body, { fontSize: 12 }]}>{data(pedido.dataCriacao)}</Text></View>
                <View style={{ paddingHorizontal: 12, paddingVertical: 8, borderRadius: 20, backgroundColor: pedido.status === 'CANCELADO' ? colors.errorBg : colors.soft }}><Text style={{ color: pedido.status === 'CANCELADO' ? colors.error : colors.primary, fontSize: 12, fontWeight: '600' }}>{labels[pedido.status] ?? pedido.status}</Text></View>
            </View>
            {isAdmin && <Text style={[ui.body, { fontSize: 13 }]}>{pedido.clienteEmail}</Text>}
            {!isAdmin && producoes[pedido.id] && <View style={{ gap: 8 }}>
                <Text style={ui.label}>Acompanhamento da produção</Text>
                <Text style={[ui.title, { color: colors.primary }]}>{labelsProducao[producoes[pedido.id].status]}</Text>
                <View style={[ui.row, { flexWrap: 'wrap', gap: 6 }]}>{etapas.map(etapa => <View key={etapa} style={{ paddingHorizontal: 8, paddingVertical: 5, borderRadius: 12, backgroundColor: etapas.indexOf(etapa) <= etapas.indexOf(producoes[pedido.id].status) ? colors.primary : colors.soft }}><Text style={{ color: etapas.indexOf(etapa) <= etapas.indexOf(producoes[pedido.id].status) ? colors.paper : colors.muted, fontSize: 10 }}>{labelsProducao[etapa]}</Text></View>)}</View>
                {(producoes[pedido.id].historico ?? []).map((evento, index) => <Text key={`${evento.status}-${index}`} style={[ui.body, { fontSize: 12 }]}>• {labelsProducao[evento.status]} · {data(evento.dataHora)}</Text>)}
                <Text style={[ui.body, { fontSize: 12 }]}>Atualizado em {data(producoes[pedido.id].dataAtualizacao)}.</Text>
            </View>}
            <View style={ui.divider} />
            {pedido.itens.map((item, index) => <View key={`${item.produtoId}-${index}`} style={{ gap: 8 }}>
                <View style={[ui.row, { alignItems: 'flex-start' }]}><Feather name="package" size={20} color={colors.accent} /><View style={{ flex: 1, gap: 5 }}><Text style={ui.label}>{item.nomeProduto}</Text><Text style={[ui.body, { fontSize: 13 }]}>{item.quantidade} {item.quantidade === 1 ? 'unidade' : 'unidades'} · {money(item.precoUnitario)} cada</Text></View><Text style={ui.label}>{money(item.subtotal)}</Text></View>
                {(item.customizacoes ?? []).map(opcao => <Text key={opcao.id} style={[ui.body, { fontSize: 13, paddingLeft: 32 }]}>{opcao.tipo}: {opcao.nome} · + {money(opcao.precoAdicional)} por unidade</Text>)}
                            {!!item.textoPersonalizado && <Text style={[ui.body, { fontSize: 13, paddingLeft: 32 }]}>Texto personalizado: “{item.textoPersonalizado}”</Text>}
                            {!!item.referenciaImagem && <Text style={[ui.body, { fontSize: 13, paddingLeft: 32 }]}>Referência da arte: {item.referenciaImagem}</Text>}
            </View>)}
            <View style={ui.divider} /><View style={[ui.row, { justifyContent: 'space-between' }]}><Text style={ui.body}>Total do pedido</Text><Text style={[ui.title, { color: colors.primary }]}>{money(pedido.valorTotal)}</Text></View>
        </View>)}
    </Screen>;
}
