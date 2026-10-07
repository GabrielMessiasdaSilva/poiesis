import React, { createContext, useState, useEffect, ReactNode } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { router } from 'expo-router';
import api from '../services/api';

type Usuario = { email: string };
type LoginResponse = { token: string; tipo?: string };

interface AuthContextData {
    signed: boolean;
    user: Usuario | null;
    loading: boolean;
    signIn: (email: string, senha: string) => Promise<void>;
    signOut: () => void;
}

export const AuthContext = createContext<AuthContextData>({} as AuthContextData);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
    const [user, setUser] = useState<any>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        async function loadStorageData() {
            try {
                const storageUser = await AsyncStorage.getItem('@Poiesis:user');
                const storageToken = await AsyncStorage.getItem('@Poiesis:token');

                if (storageUser && storageToken && !storageToken.startsWith('token-falso-')) {
                    setUser(JSON.parse(storageUser));
                } else if (storageToken?.startsWith('token-falso-')) {
                    await AsyncStorage.removeMany(['@Poiesis:user', '@Poiesis:token']);
                }
            } catch (error) {
                console.error('Erro ao carregar sessão', error);
                await AsyncStorage.removeMany(['@Poiesis:user', '@Poiesis:token']);
            } finally {
                setLoading(false);
            }
        }
        loadStorageData();
    }, []);

    async function signIn(email: string, senha: string) {
        const response = await api.post<LoginResponse>('/v1/auth/login', { email: email.trim(), senha });
        const token = response.data.token;
        if (!token) throw new Error('O servidor não retornou um token de acesso.');

        const userData = { email: email.trim() };
        await AsyncStorage.setMany({
            '@Poiesis:user': JSON.stringify(userData),
            '@Poiesis:token': token,
        });
        setUser(userData);
        router.replace('/(tabs)');
    }

    async function signOut() {
        await AsyncStorage.removeMany(['@Poiesis:user', '@Poiesis:token']);
        setUser(null);
        router.replace('/(auth)/login');
    }

    return (
        <AuthContext.Provider value={{ signed: !!user, user, loading, signIn, signOut }}>
            {children}
        </AuthContext.Provider>
    );
};
