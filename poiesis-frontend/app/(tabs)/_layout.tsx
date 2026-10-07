import { Tabs } from 'expo-router';
import { Feather } from '@expo/vector-icons';

export default function TabsLayout() {
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
        </Tabs>
    );
}