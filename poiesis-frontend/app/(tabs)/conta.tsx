import { useContext, useState } from 'react';
import { Alert, Platform, Pressable, Text, View } from 'react-native';
import { AuthContext } from '../../src/contexts/AuthContext';
export default function Conta() {
    const { user, isAdmin, signOut } = useContext(AuthContext);
    const [busy, setBusy] = useState(false);
    async function logout() {
        setBusy(true);
        try { await signOut(); }
        catch { const message = 'Você saiu deste dispositivo, mas não foi possível confirmar a revogação no servidor.'; if (Platform.OS === 'web') window.alert(message); else Alert.alert('Logout', message); }
        finally { setBusy(false); }
    }
    return <View style={{ padding: 24, gap: 16 }}>
        <Text>{user?.email}</Text><Text>Perfil: {isAdmin ? 'Administrador' : 'Usuário'}</Text>
        <Pressable accessibilityRole="button" disabled={busy} onPress={() => void logout()} style={{ padding: 16, backgroundColor: '#334B35' }}>
            <Text style={{ color: 'white' }}>{busy ? 'Saindo…' : 'Sair'}</Text>
        </Pressable>
    </View>;
}
