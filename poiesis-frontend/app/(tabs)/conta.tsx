import { useContext, useState, useRef } from 'react';
import { Text, View } from 'react-native';
import { Feather } from '@expo/vector-icons';
import { AuthContext } from '../../src/contexts/AuthContext';
import { Button, Notice, PageHeader, Screen } from '../../src/ui/components';
import { colors, ui } from '../../src/ui/theme';
export default function Conta() {
    const { user, isAdmin, signOut } = useContext(AuthContext);
    const [busy, setBusy] = useState(false);
    const [erro, setErro] = useState('');
    const trava = useRef(false);
    async function logout() {
        if (trava.current) return;
        trava.current = true; setBusy(true); setErro('');
        try { await signOut(); }
        catch { setErro('Não foi possível confirmar a saída. Verifique sua conexão e tente novamente.'); }
        finally { trava.current = false; setBusy(false); }
    }
    return <Screen narrow>
        <PageHeader eyebrow="Seu espaço" title="Minha conta." description="Confira seus dados de acesso e gerencie sua sessão." />
        <View style={ui.card}>
            <View style={[ui.row, { gap: 18 }]}><View style={{ padding: 18, backgroundColor: colors.soft, borderRadius: 40 }}><Feather name="user" size={26} color={colors.primary} /></View><View style={{ flex: 1, gap: 5 }}><Text style={ui.title}>{isAdmin ? 'Administração do ateliê' : 'Sua conta Poiesis'}</Text><Text style={ui.body}>{isAdmin ? 'Perfil de administrador' : 'Perfil de cliente'}</Text></View></View>
            <View style={ui.divider} /><Text style={ui.label}>E-mail</Text><Text selectable style={ui.body}>{user?.email}</Text>
            {!!erro && <Notice error>{erro}</Notice>}
            <Button title="Sair da conta" icon="log-out" secondary busy={busy} onPress={() => void logout()} />
        </View>
    </Screen>;
}
