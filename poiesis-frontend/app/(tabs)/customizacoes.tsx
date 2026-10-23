import { Text, View } from 'react-native';
import { router } from 'expo-router';
import { customizacoesService } from '../../src/services/produtos';
import { useConsulta } from '../../src/hooks/useConsulta';
import { Button, EmptyState, PageHeader, Screen } from '../../src/ui/components';
import { colors, money, ui } from '../../src/ui/theme';
export default function Customizacoes() {
    const { dados, carregando, erro, carregar } = useConsulta(customizacoesService.listarComProdutos, [], 'Não foi possível consultar os detalhes.');
    const grupos = dados.filter(grupo => grupo.opcoes.length);
    return <Screen narrow>
        <PageHeader eyebrow="Pequenas escolhas, sua identidade" title="O detalhe faz a peça." description="Conheça as personalizações disponíveis. Para escolher as suas, abra uma peça no catálogo." action={<Button title="Ir ao catálogo" icon="arrow-right" secondary onPress={() => router.push('/(tabs)')} />} />
        {erro ? <EmptyState title="Detalhes indisponíveis" description={erro} onRetry={() => void carregar()} /> : carregando && !dados.length ? <EmptyState title="" description="" loading /> : !grupos.length ? <EmptyState title="Novos detalhes em breve" description="Quando uma peça tiver opções de personalização, você poderá conhecê-las aqui." /> : grupos.map(({ produto, opcoes }) => <View key={produto.id} style={ui.card}>
            <Text style={ui.eyebrow}>PARA A SUA PEÇA</Text><Text style={ui.title}>{produto.nome}</Text>
            {opcoes.map(opcao => <View key={opcao.id} style={{ gap: 12 }}><View style={ui.divider} /><View style={[ui.row, { justifyContent: 'space-between', alignItems: 'flex-start' }]}><View style={{ gap: 4, flex: 1 }}><Text style={[ui.body, { fontSize: 12 }]}>{opcao.tipo}</Text><Text style={ui.label}>{opcao.nome}</Text></View><Text style={{ color: colors.primary, fontWeight: '600' }}>+ {money(opcao.precoAdicional)}</Text></View></View>)}
        </View>)}
    </Screen>;
}
