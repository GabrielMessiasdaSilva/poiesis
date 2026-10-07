import { useState } from 'react';
import { Link } from 'expo-router';
import { isAxiosError } from 'axios';
import { ActivityIndicator, Pressable, ScrollView, StyleSheet, Text, TextInput, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import api from '../../src/services/api';

export default function Cadastro() {
    const [nome, setNome] = useState('');
    const [email, setEmail] = useState('');
    const [senha, setSenha] = useState('');
    const [confirmacao, setConfirmacao] = useState('');
    const [enviando, setEnviando] = useState(false);
    const [erro, setErro] = useState('');
    const [criado, setCriado] = useState(false);

    async function cadastrar() {
        if (enviando) return;
        setErro('');
        if (!nome.trim() || !email.trim() || !senha || !confirmacao) {
            setErro('Preencha todos os campos.'); return;
        }
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
            setErro('Informe um e-mail válido.'); return;
        }
        if (senha.length < 8) { setErro('A senha deve ter pelo menos 8 caracteres.'); return; }
        if (senha !== confirmacao) { setErro('As senhas não coincidem.'); return; }
        setEnviando(true);
        try {
            await api.post('/v1/auth/register', { nome: nome.trim(), email: email.trim(), senha });
            setSenha(''); setConfirmacao(''); setCriado(true);
        } catch (error: unknown) {
            if (isAxiosError(error) && error.response?.status === 409) setErro('Este e-mail já está cadastrado. Entre com sua conta.');
            else if (isAxiosError(error) && error.response?.status === 400) setErro('Confira os dados informados e tente novamente.');
            else setErro('Não foi possível cadastrar sua conta. Confira a conexão e tente novamente.');
        } finally { setEnviando(false); }
    }

    return <SafeAreaView style={styles.safeArea}>
        <ScrollView contentContainerStyle={styles.content} keyboardShouldPersistTaps="handled">
            <View style={styles.header}><Text style={styles.brand}>POIESIS</Text><Text style={styles.title}>Crie sua conta</Text><Text style={styles.subtitle}>Comece a criar peças com a sua identidade.</Text></View>
            <View style={styles.form}>
                {criado ? <>
                    <Text accessibilityLiveRegion="polite" style={styles.success}>Conta criada com sucesso. Entre para continuar.</Text>
                    <Link href="/(auth)/login" replace style={styles.link}>Ir para o login</Link>
                </> : <>
                    <Text style={styles.label}>NOME</Text>
                    <TextInput accessibilityLabel="Nome completo" style={styles.input} value={nome} onChangeText={setNome} placeholder="Seu nome" autoComplete="name" autoCapitalize="words" maxLength={100} editable={!enviando} />
                    <Text style={styles.label}>E-MAIL</Text>
                    <TextInput accessibilityLabel="E-mail" style={styles.input} value={email} onChangeText={setEmail} placeholder="voce@email.com" autoComplete="email" autoCapitalize="none" autoCorrect={false} keyboardType="email-address" maxLength={254} editable={!enviando} />
                    <Text style={styles.label}>SENHA</Text>
                    <TextInput accessibilityLabel="Senha" style={styles.input} value={senha} onChangeText={setSenha} placeholder="Pelo menos 8 caracteres" autoComplete="new-password" autoCapitalize="none" secureTextEntry maxLength={72} editable={!enviando} />
                    <Text style={styles.label}>CONFIRMAR SENHA</Text>
                    <TextInput accessibilityLabel="Confirme a senha" style={styles.input} value={confirmacao} onChangeText={setConfirmacao} placeholder="Repita sua senha" autoComplete="new-password" autoCapitalize="none" secureTextEntry maxLength={72} editable={!enviando} />
                    {!!erro && <Text accessibilityLiveRegion="polite" style={styles.error}>{erro}</Text>}
                    <Pressable accessibilityRole="button" disabled={enviando} style={[styles.button, enviando && styles.disabled]} onPress={() => void cadastrar()}>
                        {enviando ? <ActivityIndicator color="#202522" /> : <Text style={styles.buttonText}>Criar conta</Text>}
                    </Pressable>
                    <Link href="/(auth)/login" replace style={styles.link}>Já tem uma conta? Entrar</Link>
                </>}
            </View>
        </ScrollView>
    </SafeAreaView>;
}
const styles = StyleSheet.create({
    safeArea: { flex: 1, backgroundColor: '#202522' },
    content: { flexGrow: 1, alignItems: 'center', backgroundColor: '#F3F5F2', paddingBottom: 28 },
    header: { width: '100%', maxWidth: 520, padding: 24, backgroundColor: '#202522' },
    brand: { color: '#D7F36A', fontSize: 14, fontWeight: '900', letterSpacing: 1.8 },
    title: { color: '#F6F7F4', fontSize: 30, fontWeight: '900', marginTop: 24 },
    subtitle: { color: '#A8B0A8', fontSize: 13, lineHeight: 19, marginTop: 8 },
    form: { width: '100%', maxWidth: 520, paddingHorizontal: 24, paddingTop: 12 },
    label: { color: '#465149', fontSize: 10, fontWeight: '800', letterSpacing: .8, marginTop: 16, marginBottom: 8 },
    input: { minHeight: 52, color: '#202522', fontSize: 14, paddingHorizontal: 14, backgroundColor: '#FFFFFF', borderWidth: 1, borderColor: '#DDE2DC', borderRadius: 4 },
    button: { minHeight: 52, alignItems: 'center', justifyContent: 'center', backgroundColor: '#D7F36A', borderRadius: 4, marginTop: 24 },
    buttonText: { color: '#202522', fontSize: 14, fontWeight: '900' },
    disabled: { opacity: .6 },
    link: { color: '#334B35', fontSize: 14, fontWeight: '700', textAlign: 'center', paddingVertical: 20 },
    error: { color: '#A02E25', fontSize: 13, marginTop: 16 },
    success: { color: '#334B35', fontSize: 16, lineHeight: 24, marginTop: 24 },
});
