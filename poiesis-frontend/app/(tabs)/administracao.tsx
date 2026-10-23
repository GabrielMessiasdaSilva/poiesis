import { useContext, useRef, useState } from 'react';
import { Pressable, Text, View } from 'react-native';
import { Feather } from '@expo/vector-icons';
import { relatoriosService, type RelatorioVendas, type ResumoProducao } from '../../src/services/relatorios';
import { mensagemApi } from '../../src/services/erros';
import AdminGestao from '../../src/components/AdminGestao';
import { AuthContext } from '../../src/contexts/AuthContext';
import { Button, Field, Notice, PageHeader, Screen } from '../../src/ui/components';
import { colors, money, ui } from '../../src/ui/theme';
import { periodoValido } from '../../src/services/relatorios';

export default function Administracao() {
    const { isAdmin } = useContext(AuthContext);
    const [secao, setSecao] = useState<'Produtos' | 'Produção' | 'Relatórios'>('Produtos');
    const now = new Date();
    const [inicio, setInicio] = useState(`${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-01`);
    const [fim, setFim] = useState(`${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`);
    const [resultado, setResultado] = useState<{ vendas: RelatorioVendas; producao: ResumoProducao; inicio: string; fim: string } | null>(null);
    const [erro, setErro] = useState('');
    const [busy, setBusy] = useState(false);
    const trava = useRef(false);
    async function carregar() {
        if (trava.current) return;
        setErro(''); setResultado(null);
        if (!periodoValido(inicio, fim)) { setErro('Informe datas válidas no formato AAAA-MM-DD, com a data inicial anterior ou igual à final.'); return; }
        trava.current = true; setBusy(true);
        try {
            const [vendas, producao] = await Promise.all([relatoriosService.consultarVendas(inicio, fim), relatoriosService.consultarProducao()]);
            setResultado({ vendas, producao, inicio, fim });
        } catch (error) { setErro(mensagemApi(error)); }
        finally { trava.current = false; setBusy(false); }
    }
    if (!isAdmin) return <Screen><Notice error>Acesso restrito à administração.</Notice></Screen>;
    const secoes = [
        { nome: 'Produtos' as const, icone: 'package' as const, detalhe: 'Produto, variações e editor' },
        { nome: 'Produção' as const, icone: 'scissors' as const, detalhe: 'Andamento dos pedidos' },
        { nome: 'Relatórios' as const, icone: 'bar-chart-2' as const, detalhe: 'Vendas e resultados' },
    ];
    const descricaoSecao = secoes.find(item => item.nome === secao)?.detalhe;
    return <Screen>
        <PageHeader eyebrow="PAINEL ADMINISTRATIVO" title="Gestão do ateliê." description="Cadastre produtos com suas variações e opções de personalização, acompanhe a produção e consulte os resultados." />
        <View style={{ gap: 11 }}>
            <Text style={ui.label}>Áreas de gestão</Text>
            <View style={[ui.row, { alignItems: 'stretch', gap: 10 }]}>{secoes.map(item => {
                const selecionada = secao === item.nome;
                return <Pressable key={item.nome} accessibilityRole="button" accessibilityState={{ selected: selecionada }} onPress={() => setSecao(item.nome)} style={{ flexGrow: 1, flexBasis: 190, minHeight: 78, padding: 14, borderRadius: 14, borderWidth: 1, borderColor: selecionada ? colors.primary : colors.line, backgroundColor: selecionada ? colors.soft : colors.paper, flexDirection: 'row', alignItems: 'center', gap: 12 }}>
                    <View style={{ width: 38, height: 38, borderRadius: 12, alignItems: 'center', justifyContent: 'center', backgroundColor: selecionada ? colors.paper : colors.background }}><Feather name={item.icone} size={18} color={colors.primary} /></View>
                    <View style={{ flex: 1, gap: 4 }}><Text style={{ color: colors.ink, fontWeight: '700', fontSize: 14 }}>{item.nome}</Text><Text style={{ color: colors.muted, fontSize: 11 }}>{item.detalhe}</Text></View>
                    {selecionada && <Feather name="check-circle" size={17} color={colors.primary} />}
                </Pressable>;
            })}</View>
        </View>
        <View style={{ gap: 5, paddingBottom: 4 }}><Text style={ui.eyebrow}>ÁREA SELECIONADA</Text><Text style={ui.title}>{secao}</Text><Text style={ui.body}>{descricaoSecao}</Text></View>
        {secao !== 'Relatórios' ? <AdminGestao key={secao} secao={secao} /> : <>
            <View style={ui.card}>
                <Text style={ui.title}>Um olhar sobre os pedidos</Text><Text style={ui.body}>Escolha o período para consultar os valores dos pedidos criados.</Text>
                <View style={[ui.row, { alignItems: 'flex-start' }]}>
                    <View style={{ flex: 1, minWidth: 180 }}><Field label="Data inicial" placeholder="AAAA-MM-DD" value={inicio} onChangeText={setInicio} maxLength={10} editable={!busy} /></View>
                    <View style={{ flex: 1, minWidth: 180 }}><Field label="Data final" placeholder="AAAA-MM-DD" value={fim} onChangeText={setFim} maxLength={10} editable={!busy} /></View>
                </View>
                {!!erro && <Notice error>{erro}</Notice>}
                <Button title="Consultar relatórios" icon="bar-chart-2" busy={busy} onPress={() => void carregar()} />
            </View>
            {resultado && <>
                <Text style={ui.body}>Período consultado: {resultado.inicio.split('-').reverse().join('/')} a {resultado.fim.split('-').reverse().join('/')}</Text>
                <View style={[ui.row, { alignItems: 'stretch' }]}>{[['Pedidos criados', String(resultado.vendas.totalPedidos)], ['Valor dos pedidos', money(resultado.vendas.faturamentoTotal)], ['Ticket médio', money(resultado.vendas.ticketMedio)]].map(([label, valor]) => <View key={label} style={[ui.card, { flex: 1, minWidth: 200 }]}><Text style={ui.body}>{label}</Text><Text style={ui.title}>{valor}</Text></View>)}</View>
                <View style={ui.card}><Text style={ui.title}>Produção agora</Text><Text style={ui.body}>Resumo atual das ordens, independente do período de vendas.</Text>
                    {([['Pendentes', resultado.producao.emPendente], ['Em corte', resultado.producao.emCorte], ['Em costura', resultado.producao.emCostura], ['Em acabamento', resultado.producao.emAcabamento], ['Concluídas', resultado.producao.concluidos]] as const).map(([label, valor]) => <View key={label} style={[ui.row, { justifyContent: 'space-between', paddingVertical: 8, borderBottomWidth: 1, borderBottomColor: colors.line }]}><Text style={ui.body}>{label}</Text><Text style={ui.label}>{valor}</Text></View>)}
                </View>
            </>}
        </>}
    </Screen>;
}
