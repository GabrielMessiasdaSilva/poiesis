import { Stack } from 'expo-router';
import { ActivityIndicator, StyleSheet, View } from 'react-native';
import { AuthContext, AuthProvider } from '../src/contexts/AuthContext';
import { useContext } from 'react';

function RootNavigator() {
    const { signed, loading } = useContext(AuthContext);
    if (loading) return <View style={styles.loading}><ActivityIndicator color="#334B35" /></View>;
    return (
        <Stack screenOptions={{ headerShown: false }}>
            <Stack.Protected guard={!signed}><Stack.Screen name="(auth)/login" /></Stack.Protected>
            <Stack.Protected guard={signed}><Stack.Screen name="(tabs)" /></Stack.Protected>
        </Stack>
    );
}
export default function RootLayout() {
    return <AuthProvider><RootNavigator /></AuthProvider>;
}
const styles = StyleSheet.create({ loading: { flex: 1, alignItems: 'center', justifyContent: 'center', backgroundColor: '#F3F5F2' } });
