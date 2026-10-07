import { router, Stack, useSegments } from 'expo-router';
import { ActivityIndicator, StyleSheet, View } from 'react-native';
import { AuthContext, AuthProvider } from '../src/contexts/AuthContext';
import { useContext, useEffect } from 'react';

function RootNavigator() {
    const { signed, loading } = useContext(AuthContext);
    const firstSegment = useSegments()[0];

    useEffect(() => {
        if (loading || !firstSegment) return;

        if (signed && firstSegment !== '(tabs)') {
            router.replace('/(tabs)');
        } else if (!signed && firstSegment !== '(auth)') {
            router.replace('/(auth)/login');
        }
    }, [firstSegment, loading, signed]);

    if (loading) {
        return (
            <View style={styles.loading}>
                <ActivityIndicator color="#334B35" />
            </View>
        );
    }

    return (
        <Stack initialRouteName={signed ? '(tabs)' : '(auth)/login'} screenOptions={{ headerShown: false }}>
            <Stack.Screen name="(auth)/login" />
            <Stack.Screen name="(tabs)" />
        </Stack>
    );
}

export default function RootLayout() {
    return (
        <AuthProvider>
            <RootNavigator />
        </AuthProvider>
    );
}

const styles = StyleSheet.create({
    loading: { flex: 1, alignItems: 'center', justifyContent: 'center', backgroundColor: '#F3F5F2' },
});