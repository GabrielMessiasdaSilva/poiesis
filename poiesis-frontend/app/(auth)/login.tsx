import { Link } from 'expo-router';
import { useState, useContext, useRef } from 'react';
import { Text, View } from 'react-native';
import { isAxiosError } from 'axios';
import { AuthContext } from '../../src/contexts/AuthContext';
import { mensagemApi } from '../../src/services/erros';
import { AuthShell, Button, Field, Notice } from '../../src/ui/components';
import { colors, serif, ui } from '../../src/ui/theme';

export default function Login() {
    const [email, setEmail] = useState('');
    const [senha, setSenha] = useState('');
    const [enviando, setEnviando] = useState(false);
    const [erro, setErro] = useState('');
    const trava = useRef(false);
    const { signIn } = useContext(AuthContext);
    async function entrar() {
        if (trava.current) return;
        setErro('');
        if (!email.trim() || !senha) { setErro('Preencha seu e-mail e sua senha.'); return; }
        trava.current = true; setEnviando(true);
        try { await signIn(email, senha); }
        catch (error) { setErro(isAxiosError(error) && error.response?.status === 401 ? 'E-mail ou senha inválidos.' : mensagemApi(error)); }
        finally { trava.current = false; setEnviando(false); }
    }
    return <AuthShell>
        <View style={{ gap: 10 }}>
            <Text style={ui.eyebrow}>BEM-VINDO AO SEU ATELIÊ</Text>
            <Text accessibilityRole="header" style={{ fontFamily: serif, color: colors.ink, fontSize: 34 }}>Que bom ter você aqui.</Text>
            <Text style={ui.body}>Entre para escolher suas peças e acompanhar cada pedido.</Text>
        </View>
        <Field label="E-mail" value={email} onChangeText={setEmail} placeholder="voce@email.com" autoComplete="email" keyboardType="email-address" autoCapitalize="none" autoCorrect={false} editable={!enviando} />
        <Field label="Senha" value={senha} onChangeText={setSenha} placeholder="Sua senha" autoComplete="current-password" secureTextEntry autoCapitalize="none" editable={!enviando} onSubmitEditing={() => void entrar()} returnKeyType="go" />
        {!!erro && <Notice error>{erro}</Notice>}
        <Button title="Entrar na minha conta" icon="arrow-right" busy={enviando} onPress={() => void entrar()} />
        <View style={ui.divider} />
        <Text style={[ui.body, { textAlign: 'center' }]}>Primeira vez por aqui?</Text>
        <Link href="/(auth)/cadastro" style={{ textAlign: 'center', color: colors.primary, fontSize: 15, fontWeight: '600', paddingVertical: 12 }}>Criar uma conta</Link>
    </AuthShell>;
}
