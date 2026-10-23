import { useRef, useState } from 'react';
import { Link } from 'expo-router';
import { isAxiosError } from 'axios';
import { Text, View } from 'react-native';
import api from '../../src/services/api';
import { mensagemApi } from '../../src/services/erros';
import { AuthShell, Button, Field, Notice } from '../../src/ui/components';
import { colors, serif, ui } from '../../src/ui/theme';

export default function Cadastro() {
    const [nome, setNome] = useState('');
    const [email, setEmail] = useState('');
    const [senha, setSenha] = useState('');
    const [confirmacao, setConfirmacao] = useState('');
    const [enviando, setEnviando] = useState(false);
    const [erro, setErro] = useState('');
    const [criado, setCriado] = useState(false);
    const trava = useRef(false);
    async function cadastrar() {
        if (trava.current) return;
        setErro('');
        if (!nome.trim() || !email.trim() || !senha || !confirmacao) { setErro('Preencha todos os campos.'); return; }
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) { setErro('Informe um e-mail válido.'); return; }
        if (senha.length < 8) { setErro('A senha deve ter pelo menos 8 caracteres.'); return; }
        if (senha !== confirmacao) { setErro('As senhas não coincidem.'); return; }
        trava.current = true; setEnviando(true);
        try {
            await api.post('/v1/auth/register', { nome: nome.trim(), email: email.trim(), senha });
            setSenha(''); setConfirmacao(''); setCriado(true);
        } catch (error) {
            setErro(isAxiosError(error) && error.response?.status === 409 ? 'Este e-mail já está cadastrado. Entre com sua conta.' : mensagemApi(error));
        } finally { trava.current = false; setEnviando(false); }
    }
    return <AuthShell>
        <View style={{ gap: 10 }}><Text style={ui.eyebrow}>SEU PRIMEIRO PASSO</Text><Text accessibilityRole="header" style={{ fontFamily: serif, color: colors.ink, fontSize: 34 }}>Crie sua conta.</Text><Text style={ui.body}>Um espaço para suas escolhas, do detalhe ao pedido.</Text></View>
        {criado ? <Notice>Conta criada com sucesso. Entre para começar a escolher suas peças.</Notice> : <>
            <Field label="Nome completo" value={nome} onChangeText={setNome} placeholder="Como podemos chamar você?" autoComplete="name" autoCapitalize="words" maxLength={100} editable={!enviando} />
            <Field label="E-mail" value={email} onChangeText={setEmail} placeholder="voce@email.com" autoComplete="email" autoCapitalize="none" autoCorrect={false} keyboardType="email-address" maxLength={254} editable={!enviando} />
            <Field label="Senha" hint="Use pelo menos 8 caracteres." value={senha} onChangeText={setSenha} placeholder="Crie uma senha" autoComplete="new-password" autoCapitalize="none" secureTextEntry maxLength={72} editable={!enviando} />
            <Field label="Confirmar senha" value={confirmacao} onChangeText={setConfirmacao} placeholder="Repita sua senha" autoComplete="new-password" autoCapitalize="none" secureTextEntry maxLength={72} editable={!enviando} onSubmitEditing={() => void cadastrar()} />
            {!!erro && <Notice error>{erro}</Notice>}
            <Button title="Criar minha conta" busy={enviando} onPress={() => void cadastrar()} />
        </>}
        <Link href="/(auth)/login" replace style={{ textAlign: 'center', color: colors.primary, fontSize: 15, fontWeight: '600', paddingVertical: 12 }}>{criado ? 'Entrar na minha conta' : 'Já tenho uma conta'}</Link>
    </AuthShell>;
}
