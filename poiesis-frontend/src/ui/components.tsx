import { useState, type ReactNode } from 'react';
import { ActivityIndicator, KeyboardAvoidingView, Platform, Pressable, ScrollView, StyleSheet, Text, TextInput, View, useWindowDimensions, type TextInputProps } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Feather, MaterialCommunityIcons } from '@expo/vector-icons';
import { colors, serif, ui } from './theme';

export function Button({ title, onPress, busy = false, disabled = false, secondary = false, danger = false, icon }: {
    title: string; onPress: () => void; busy?: boolean; disabled?: boolean; secondary?: boolean; danger?: boolean; icon?: keyof typeof Feather.glyphMap;
}) {
    const blocked = disabled || busy;
    const color = secondary ? (danger ? colors.error : colors.primary) : colors.paper;
    return <Pressable accessibilityRole="button" accessibilityLabel={title} accessibilityState={{ disabled: blocked, busy }} disabled={blocked} onPress={onPress}
        style={({ pressed }) => [styles.button, { backgroundColor: secondary ? colors.paper : danger ? colors.error : colors.primary, borderColor: secondary ? colors.line : 'transparent', opacity: blocked ? .55 : pressed ? .8 : 1 }]}>
        {busy ? <ActivityIndicator size="small" color={color} /> : icon ? <Feather name={icon} size={17} color={color} /> : null}
        <Text style={{ color, fontSize: 14, fontWeight: '600', textAlign: 'center', flexShrink: 1 }}>{title}</Text>
    </Pressable>;
}
export function Field({ label, hint, secureTextEntry, ...props }: TextInputProps & { label: string; hint?: string }) {
    const [focus, setFocus] = useState(false);
    const [visible, setVisible] = useState(false);
    return <View style={{ gap: 8, minWidth: 0 }}>
        <Text style={ui.label}>{label}</Text>
        <View style={[styles.inputRow, focus && { borderColor: colors.primary, backgroundColor: '#FFF' }]}>
            <TextInput {...props} accessibilityLabel={props.accessibilityLabel ?? label} placeholderTextColor={colors.muted}
                secureTextEntry={secureTextEntry && !visible} onFocus={() => setFocus(true)} onBlur={() => setFocus(false)}
                style={[styles.input, props.multiline && { minHeight: 88, textAlignVertical: 'top' }, props.style]} />
            {secureTextEntry && <Pressable accessibilityRole="button" accessibilityLabel={visible ? `Ocultar ${label.toLowerCase()}` : `Mostrar ${label.toLowerCase()}`} onPress={() => setVisible(!visible)} style={styles.eye}>
                <Feather name={visible ? 'eye-off' : 'eye'} size={19} color={colors.muted} />
            </Pressable>}
        </View>
        {hint && <Text style={{ color: colors.muted, fontSize: 12, lineHeight: 18 }}>{hint}</Text>}
    </View>;
}
export function Notice({ children, error = false }: { children: ReactNode; error?: boolean }) {
    return <View style={[styles.notice, { backgroundColor: error ? colors.errorBg : colors.successBg }]}>
        <Feather name={error ? 'alert-circle' : 'check-circle'} size={18} color={error ? colors.error : colors.success} />
        <Text accessibilityRole={error ? 'alert' : undefined} accessibilityLiveRegion="polite" style={{ flex: 1, color: error ? colors.error : colors.success, fontSize: 14, lineHeight: 21 }}>{children}</Text>
    </View>;
}
export function Screen({ children, narrow = false }: { children: ReactNode; narrow?: boolean }) {
    const { width } = useWindowDimensions();
    return <ScrollView style={{ flex: 1, backgroundColor: colors.background }} keyboardShouldPersistTaps="handled" contentContainerStyle={{ flexGrow: 1 }}>
        <View style={{ width: '100%', maxWidth: narrow ? 760 : 1200, alignSelf: 'center', padding: width < 600 ? 20 : 40, paddingBottom: 48, gap: 28 }}>{children}</View>
    </ScrollView>;
}
export function PageHeader({ eyebrow, title, description, action }: { eyebrow: string; title: string; description?: string; action?: ReactNode }) {
    const { width } = useWindowDimensions();
    return <View style={{ gap: 12 }}>
        <Text style={ui.eyebrow}>{eyebrow.toUpperCase()}</Text>
        <View style={[ui.row, { justifyContent: 'space-between', alignItems: 'flex-start', flexDirection: width < 600 ? 'column' : 'row' }]}>
            <View style={{ flex: width < 600 ? undefined : 1, width: width < 600 ? '100%' : undefined, minWidth: 180, gap: 10 }}>
                <Text accessibilityRole="header" style={{ color: colors.ink, fontFamily: serif, fontSize: width < 600 ? 34 : 44, lineHeight: width < 600 ? 40 : 52 }}>{title}</Text>
                {description && <Text style={[ui.body, { maxWidth: 620 }]}>{description}</Text>}
            </View>
            {action}
        </View>
    </View>;
}
export function EmptyState({ title, description, loading = false, onRetry }: { title: string; description: string; loading?: boolean; onRetry?: () => void }) {
    return <View style={[ui.card, { alignItems: 'center', paddingVertical: 40 }]}>
        {loading ? <ActivityIndicator color={colors.primary} /> : <Feather name={onRetry ? 'wifi-off' : 'inbox'} size={28} color={colors.accent} />}
        <Text style={[ui.title, { textAlign: 'center' }]}>{loading ? 'Só um instante…' : title}</Text>
        <Text style={[ui.body, { maxWidth: 400, textAlign: 'center' }]}>{loading ? 'Estamos buscando seus dados.' : description}</Text>
        {onRetry && !loading && <Button title="Tentar novamente" onPress={onRetry} secondary />}
    </View>;
}
export function AuthShell({ children }: { children: ReactNode }) {
    const { width } = useWindowDimensions();
    const wide = width >= 900;
    return <SafeAreaView style={{ flex: 1, backgroundColor: colors.background }}>
        <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : undefined} style={{ flex: 1 }}>
            <ScrollView keyboardShouldPersistTaps="handled" contentContainerStyle={{ flexGrow: 1, justifyContent: 'center', padding: wide ? 40 : 20 }}>
                <View style={{ flexDirection: wide ? 'row' : 'column', width: '100%', maxWidth: 1120, alignSelf: 'center', borderRadius: 24, overflow: 'hidden', backgroundColor: colors.paper, borderWidth: 1, borderColor: colors.line }}>
                    <View style={{ width: wide ? '46%' : '100%', backgroundColor: colors.ink, padding: wide ? 48 : 28, justifyContent: 'space-between', gap: wide ? 48 : 24 }}>
                        <Text style={{ color: '#E9D6BE', fontSize: 22, letterSpacing: 6, fontWeight: '500' }}>POIESIS</Text>
                        {wide && <View style={{ alignSelf: 'center', width: 220, height: 220, borderRadius: 110, borderWidth: 1, borderColor: '#80624B', alignItems: 'center', justifyContent: 'center' }}>
                            <View style={{ width: 184, height: 184, borderRadius: 92, borderWidth: 1, borderStyle: 'dashed', borderColor: '#80624B', alignItems: 'center', justifyContent: 'center' }}><MaterialCommunityIcons name="tshirt-crew-outline" size={100} color="#D4B28E" /></View>
                        </View>}
                        <View style={{ gap: 14 }}>
                            <Text style={{ color: '#F7EDE1', fontFamily: serif, fontSize: wide ? 44 : 30, lineHeight: wide ? 51 : 36 }}>Peças simples.{'\n'}Histórias suas.</Text>
                            <Text style={{ color: '#D0BDAA', fontSize: 14, lineHeight: 23, maxWidth: 300 }}>Escolha a base, encontre os detalhes e dê forma ao seu jeito de vestir.</Text>
                        </View>
                        {wide && <Text style={{ color: '#BEA58D', fontSize: 11, letterSpacing: 2 }}>DO PRIMEIRO DETALHE À SUA PEÇA.</Text>}
                    </View>
                    <View style={{ flex: 1, padding: wide ? 48 : 24, gap: 22, justifyContent: 'center' }}>{children}</View>
                </View>
            </ScrollView>
        </KeyboardAvoidingView>
    </SafeAreaView>;
}
const styles = StyleSheet.create({
    button: { minHeight: 48, borderRadius: 10, borderWidth: 1, paddingHorizontal: 19, paddingVertical: 12, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 9 },
    inputRow: { flexDirection: 'row', alignItems: 'center', backgroundColor: colors.paper, borderWidth: 1, borderColor: colors.line, borderRadius: 10, overflow: 'hidden' },
    input: { flex: 1, minWidth: 0, minHeight: 50, padding: 14, color: colors.ink, fontSize: 15 },
    eye: { width: 48, minHeight: 48, alignItems: 'center', justifyContent: 'center' },
    notice: { flexDirection: 'row', alignItems: 'flex-start', gap: 10, padding: 16, borderRadius: 10 },
});
