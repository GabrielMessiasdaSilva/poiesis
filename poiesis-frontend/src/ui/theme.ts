import { Platform, StyleSheet } from 'react-native';

export const colors = {
    background: '#F7F2EB', paper: '#FFFCF8', ink: '#38281F', muted: '#756457',
    primary: '#704A35', accent: '#AC7653', line: '#E2D5C7', soft: '#EFE4D7',
    success: '#486044', successBg: '#ECF1E7', error: '#963E32', errorBg: '#FAEDE7',
};
export const serif = Platform.select({ ios: 'Georgia', android: 'serif', default: 'Georgia, serif' });
export const money = (value: number) => Number(value).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
export const ui = StyleSheet.create({
    card: { padding: 24, gap: 16, borderWidth: 1, borderColor: colors.line, borderRadius: 16, backgroundColor: colors.paper },
    row: { flexDirection: 'row', alignItems: 'center', flexWrap: 'wrap', gap: 12 },
    body: { color: colors.muted, fontSize: 15, lineHeight: 23 },
    title: { color: colors.ink, fontSize: 23, fontFamily: serif },
    label: { color: colors.ink, fontSize: 14, fontWeight: '600' },
    eyebrow: { color: colors.primary, fontSize: 11, fontWeight: '700', letterSpacing: 2 },
    divider: { height: 1, backgroundColor: colors.line },
    chip: { minHeight: 44, paddingHorizontal: 16, paddingVertical: 11, borderWidth: 1, borderColor: colors.line, borderRadius: 24, backgroundColor: colors.paper },
    selected: { backgroundColor: colors.soft, borderColor: colors.primary },
});
