import { Tabs } from 'expo-router';
import { useContext } from 'react';
import { Text, View, useWindowDimensions } from 'react-native';
import { Feather } from '@expo/vector-icons';
import { AuthContext } from '../../src/contexts/AuthContext';
import { colors } from '../../src/ui/theme';

export default function TabsLayout() {
    const { isAdmin } = useContext(AuthContext);
    const { width } = useWindowDimensions();
    const desktop = width >= 1000;
    return <Tabs screenOptions={{
        headerTitle: () => <View style={{ gap: 3 }}><Text style={{ color: colors.ink, fontSize: 18, letterSpacing: 5 }}>POIESIS</Text></View>,
        headerStyle: { backgroundColor: colors.paper }, headerShadowVisible: false,
        headerTitleAlign: 'left', headerTintColor: colors.ink,
        sceneStyle: { backgroundColor: colors.background },
        tabBarPosition: desktop ? 'left' : 'bottom',
        tabBarActiveTintColor: colors.primary, tabBarInactiveTintColor: colors.muted,
        tabBarActiveBackgroundColor: colors.soft,
        tabBarLabelPosition: desktop ? 'beside-icon' : 'below-icon',
        tabBarLabelStyle: { fontSize: desktop ? 14 : 10, fontWeight: '600' },
        tabBarStyle: { backgroundColor: colors.paper, borderColor: colors.line, ...(desktop ? { width: 210, maxWidth: 210, paddingTop: 24, paddingHorizontal: 12 } : { minHeight: 68, paddingTop: 6, paddingBottom: 8 }) },
        tabBarItemStyle: { borderRadius: 10, ...(desktop ? { height: 52, marginBottom: 8, justifyContent: 'flex-start' } : { marginHorizontal: 3 }) },
    }}>
        <Tabs.Screen name="index" options={{ title: 'Catálogo', tabBarAccessibilityLabel: 'Catálogo', tabBarIcon: ({ color }) => <Feather name="grid" size={20} color={color} /> }} />
        <Tabs.Screen name="customizacoes" options={{ title: 'Detalhes', tabBarAccessibilityLabel: 'Detalhes', tabBarIcon: ({ color }) => <Feather name="sliders" size={20} color={color} /> }} />
        <Tabs.Screen name="pedidos" options={{ title: 'Pedidos', tabBarAccessibilityLabel: 'Pedidos', tabBarIcon: ({ color }) => <Feather name="shopping-bag" size={20} color={color} /> }} />
        <Tabs.Protected guard={isAdmin}><Tabs.Screen name="administracao" options={{ title: desktop ? 'Administração' : 'Gestão', tabBarAccessibilityLabel: desktop ? 'Administração' : 'Gestão', tabBarIcon: ({ color }) => <Feather name="bar-chart-2" size={20} color={color} /> }} /></Tabs.Protected>
        <Tabs.Screen name="conta" options={{ title: 'Minha conta', tabBarAccessibilityLabel: 'Minha conta', tabBarIcon: ({ color }) => <Feather name="user" size={20} color={color} /> }} />
    </Tabs>;
}
