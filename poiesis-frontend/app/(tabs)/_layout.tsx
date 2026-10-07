import { Tabs } from 'expo-router';
import { useContext } from 'react';
import { AuthContext } from '../../src/contexts/AuthContext';
import { Feather } from '@expo/vector-icons';

export default function TabsLayout() {
    const { isAdmin } = useContext(AuthContext);
    return (
        <Tabs screenOptions={{ tabBarActiveTintColor: '#000' }}>
            <Tabs.Screen
                name="index"
                options={{
                    title: 'Catálogo',
                    tabBarIcon: ({ color }) => <Feather name="grid" size={24} color={color} />
                }}
            />
            <Tabs.Screen
                name="customizacoes"
                options={{
                    title: 'Customizações',
                    tabBarIcon: ({ color }) => <Feather name="edit-2" size={24} color={color} />
                }}
            />
            <Tabs.Screen
                name="pedidos"
                options={{
                    title: 'Pedidos',
                    tabBarIcon: ({ color }) => <Feather name="shopping-bag" size={24} color={color} />
                }}
            />
            <Tabs.Protected guard={isAdmin}>
                <Tabs.Screen name="administracao" options={{ title: 'Administração', tabBarIcon: ({ color }) => <Feather name="bar-chart-2" size={24} color={color} /> }} />
            </Tabs.Protected>
            <Tabs.Screen name="conta" options={{ title: 'Conta', tabBarIcon: ({ color }) => <Feather name="user" size={24} color={color} /> }} />
        </Tabs>
    );
}