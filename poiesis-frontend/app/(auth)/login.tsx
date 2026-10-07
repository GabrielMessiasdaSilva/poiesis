import React, { useState, useContext } from 'react';
import { ActivityIndicator, Alert, Pressable, ScrollView, StyleSheet, Text, TextInput, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Feather } from '@expo/vector-icons';
import { AuthContext } from '../../src/contexts/AuthContext';

export default function Login() {
    const [email, setEmail] = useState('');
    const [senha, setSenha] = useState('');
    const [enviando, setEnviando] = useState(false);
    const { signIn } = useContext(AuthContext);

    async function handleLogin() {
        if (!email || !senha) return Alert.alert('Erro', 'Preencha todos os campos');

        setEnviando(true);
        try {
            await signIn(email, senha);
        } catch (error: any) {
            const status = error?.response?.status;
            Alert.alert('Não foi possível entrar', status === 401
                ? 'E-mail ou senha inválidos.'
                : 'Não foi possível conectar ao servidor. Confira se o backend está ativo.');
        } finally {
            setEnviando(false);
        }
    }

    return (
        <SafeAreaView style={styles.safeArea}>
            <ScrollView contentContainerStyle={styles.scrollContent} keyboardShouldPersistTaps="handled">
                <View style={styles.hero}>
                    <View style={styles.heroTopline}>
                        <View style={styles.brandMark}><Feather name="activity" size={19} color="#D7F36A" /></View>
                        <Text style={styles.brandName}>POIESIS</Text>
                        <Text style={styles.brandDescriptor}>CUSTOM WEAR / 01</Text>
                    </View>
                    <View style={styles.heroCopy}>
                        <Text style={styles.heroEyebrow}>VISTA O QUE MOVE VOCÊ</Text>
                        <Text style={styles.heroTitle}>Seu jogo.{ '\n' }Seu jeito.</Text>
                        <View style={styles.heroUnderline} />
                    </View>
                    <View style={styles.trackMarks}>
                        <View style={styles.trackLine} />
                        <View style={[styles.trackLine, styles.trackLineShort]} />
                        <View style={[styles.trackLine, styles.trackLineMedium]} />
                    </View>
                </View>

                <View style={styles.form}>
                    <Text style={styles.formEyebrow}>ÁREA DO ATLETA</Text>
                    <Text style={styles.formTitle}>Acesse sua conta</Text>
                    <Text style={styles.formSubtitle}>Entre para continuar criando peças com a sua identidade.</Text>

                    <Text style={styles.fieldLabel}>E-MAIL</Text>
                    <View style={styles.inputRow}>
                        <Feather name="mail" size={17} color="#748078" />
                        <TextInput
                            style={styles.input}
                            placeholder="voce@email.com"
                            placeholderTextColor="#929A93"
                            value={email}
                            onChangeText={setEmail}
                            autoCapitalize="none"
                            autoComplete="email"
                            keyboardType="email-address"
                        />
                    </View>

                    <Text style={[styles.fieldLabel, styles.passwordLabel]}>SENHA</Text>
                    <View style={styles.inputRow}>
                        <Feather name="lock" size={17} color="#748078" />
                        <TextInput
                            style={styles.input}
                            placeholder="Sua senha"
                            placeholderTextColor="#929A93"
                            value={senha}
                            onChangeText={setSenha}
                            secureTextEntry
                            autoComplete="password"
                        />
                    </View>

                    <Pressable accessibilityRole="button" disabled={enviando} style={styles.button} onPress={handleLogin}>
                        {enviando ? <ActivityIndicator color="#202522" /> : <>
                            <Text style={styles.buttonText}>Entrar</Text>
                            <Feather name="arrow-right" size={18} color="#202522" />
                        </>}
                    </Pressable>

                    <View style={styles.demoNote}>
                        <View style={styles.demoDot} />
                        <Text style={styles.demoText}>ACESSO SEGURO PELO SERVIDOR POIESIS</Text>
                    </View>
                </View>
                <Text style={styles.footer}>POIESIS · FEITO PARA O SEU MOVIMENTO</Text>
            </ScrollView>
        </SafeAreaView>
    );
}

const styles = StyleSheet.create({
    safeArea: { flex: 1, backgroundColor: '#202522' },
    scrollContent: { flexGrow: 1, alignItems: 'center', backgroundColor: '#F3F5F2', paddingBottom: 22 },
    hero: { width: '100%', maxWidth: 520, minHeight: 278, paddingHorizontal: 24, paddingTop: 22, paddingBottom: 25, overflow: 'hidden', backgroundColor: '#202522' },
    heroTopline: { flexDirection: 'row', alignItems: 'center' },
    brandMark: { width: 34, height: 34, borderWidth: 1, borderColor: '#536052', alignItems: 'center', justifyContent: 'center', marginRight: 10 },
    brandName: { color: '#F6F7F4', fontSize: 14, fontWeight: '900', letterSpacing: 1.8 },
    brandDescriptor: { color: '#A8B0A8', fontSize: 9, fontWeight: '800', letterSpacing: 0.8, marginLeft: 'auto' },
    heroCopy: { marginTop: 35 },
    heroEyebrow: { color: '#D7F36A', fontSize: 10, fontWeight: '800', letterSpacing: 1.5 },
    heroTitle: { color: '#F6F7F4', fontSize: 42, lineHeight: 44, fontWeight: '900', marginTop: 9 },
    heroUnderline: { width: 42, height: 4, backgroundColor: '#D7F36A', marginTop: 14 },
    trackMarks: { position: 'absolute', right: 24, bottom: 28, gap: 5, transform: [{ rotate: '-35deg' }] },
    trackLine: { width: 74, height: 3, backgroundColor: '#52604F' },
    trackLineShort: { width: 48 },
    trackLineMedium: { width: 62 },
    form: { width: '100%', maxWidth: 520, paddingHorizontal: 24, paddingTop: 29 },
    formEyebrow: { color: '#667068', fontSize: 10, fontWeight: '800', letterSpacing: 1.2 },
    formTitle: { color: '#202522', fontSize: 26, fontWeight: '900', marginTop: 7 },
    formSubtitle: { color: '#667068', fontSize: 13, lineHeight: 19, marginTop: 6, marginBottom: 25 },
    fieldLabel: { color: '#465149', fontSize: 10, fontWeight: '800', letterSpacing: 0.8, marginBottom: 8 },
    passwordLabel: { marginTop: 16 },
    inputRow: { minHeight: 52, flexDirection: 'row', alignItems: 'center', gap: 11, paddingHorizontal: 14, backgroundColor: '#FFFFFF', borderWidth: 1, borderColor: '#DDE2DC', borderRadius: 4 },
    input: { flex: 1, minHeight: 50, color: '#202522', fontSize: 14 },
    button: { minHeight: 52, flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', paddingHorizontal: 17, backgroundColor: '#D7F36A', borderRadius: 4, marginTop: 23 },
    buttonText: { color: '#202522', fontSize: 14, fontWeight: '900' },
    demoNote: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 8, marginTop: 20 },
    demoDot: { width: 7, height: 7, borderRadius: 4, backgroundColor: '#85A866' },
    demoText: { color: '#667068', fontSize: 9, fontWeight: '800', letterSpacing: 1 },
    footer: { width: '100%', maxWidth: 520, color: '#89918A', fontSize: 9, textAlign: 'center', fontWeight: '700', letterSpacing: 0.8, marginTop: 'auto', paddingHorizontal: 20, paddingTop: 24 },
});
