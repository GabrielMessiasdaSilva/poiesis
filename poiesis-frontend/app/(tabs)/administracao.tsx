import { useContext, useState } from 'react';
import { ScrollView, Text, TextInput, Pressable, ActivityIndicator, View } from 'react-native';
import api from '../../src/services/api';
import AdminGestao from '../../src/components/AdminGestao';
import { AuthContext } from '../../src/contexts/AuthContext';

type Producao = { emPendente: number; emCorte: number; emCostura: number; emAcabamento: number; concluidos: number };
type Vendas = { totalPedidos: number; faturamentoTotal: number };
export default function Administracao() {
    const { isAdmin } = useContext(AuthContext);
    const [secao, setSecao] = useState<'Produtos' | 'Customizações' | 'Produção' | 'Relatórios'>('Produtos');
    const now = new Date();
    const [inicio, setInicio] = useState(`${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-01`);
    const [fim, setFim] = useState(`${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`);
    const [vendas, setVendas] = useState<Vendas | null>(null);
    const [producao, setProducao] = useState<Producao | null>(null);
    const [erro, setErro] = useState('');
    const [busy, setBusy] = useState(false);
    async function carregar() {
        setBusy(true); setErro(''); setVendas(null); setProducao(null);
        try {
            const [v, p] = await Promise.all([
                api.get<Vendas>('/v1/relatorios/vendas', { params: { dataInicio: inicio, dataFim: fim } }),
                api.get<Producao>('/v1/relatorios/producao'),
            ]);
            setVendas(v.data); setProducao(p.data);
        } catch { setErro('Não foi possível consultar os relatórios. Confira as datas e tente novamente.'); }
        finally { setBusy(false); }
    }
    if (!isAdmin) return <Text>Acesso restrito à administração.</Text>;
    return <ScrollView keyboardShouldPersistTaps="handled" style={{ backgroundColor: '#F3F5F2' }} contentContainerStyle={{ padding: 24, gap: 16, width: '100%', maxWidth: 760, alignSelf: 'center', paddingBottom: 48 }}>
        <Text style={{ fontSize: 26, fontWeight: '700' }}>Administração</Text>
        <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 8 }}>{(['Produtos', 'Customizações', 'Produção', 'Relatórios'] as const).map(s => <Pressable key={s} accessibilityRole="button" accessibilityState={{ selected: secao === s }} onPress={() => setSecao(s)} style={{ padding: 12, minHeight: 44, borderRadius: 5, backgroundColor: secao === s ? '#334B35' : '#E2E6E1' }}><Text style={{ color: secao === s ? 'white' : '#202522' }}>{s}</Text></Pressable>)}</View>
        {secao !== 'Relatórios' ? <AdminGestao key={secao} secao={secao} /> : <>
        <Text style={{ fontSize: 24 }}>Relatórios</Text>
        <Text>Período de vendas (AAAA-MM-DD)</Text>
        <TextInput accessibilityLabel="Data inicial" value={inicio} onChangeText={setInicio} style={{ borderWidth: 1, padding: 12 }} />
        <TextInput accessibilityLabel="Data final" value={fim} onChangeText={setFim} style={{ borderWidth: 1, padding: 12 }} />
        <Pressable accessibilityRole="button" onPress={() => void carregar()} disabled={busy} style={{ padding: 16, backgroundColor: '#334B35' }}><Text style={{ color: 'white' }}>Consultar relatórios</Text></Pressable>
        {busy && <ActivityIndicator />}{erro && <Text>{erro}</Text>}
        {vendas && <Text>Pedidos: {vendas.totalPedidos}{'\n'}Valor de pedidos criados: R$ {Number(vendas.faturamentoTotal).toFixed(2)}</Text>}
        {producao && <Text>Produção atual{'\n'}Pendentes: {producao.emPendente}{'\n'}Em corte: {producao.emCorte}{'\n'}Em costura: {producao.emCostura}{'\n'}Em acabamento: {producao.emAcabamento}{'\n'}Concluídos: {producao.concluidos}</Text>}
        </>}
    </ScrollView>;
}
