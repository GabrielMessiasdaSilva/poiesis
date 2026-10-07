import React, { createContext, useState, useEffect, useCallback, useRef, ReactNode } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { AppState, Platform } from 'react-native';
import api, { setUnauthorizedHandler } from '../services/api';

type Usuario = { email: string; roles: string[]; expiresAt: number };
interface AuthContextData {
    signed: boolean;
    isAdmin: boolean;
    user: Usuario | null;
    loading: boolean;
    signIn: (email: string, senha: string) => Promise<void>;
    signOut: () => Promise<void>;
}
export const AuthContext = createContext<AuthContextData>({} as AuthContextData);
const keys = ['@Poiesis:user', '@Poiesis:token'];
function validSession(data: Usuario) {
    return typeof data.email === 'string' && Array.isArray(data.roles)
        && Number.isFinite(data.expiresAt) && data.expiresAt > Date.now();
}
export const AuthProvider = ({ children }: { children: ReactNode }) => {
    const [user, setUser] = useState<Usuario | null>(null);
    const [loading, setLoading] = useState(true);
    const currentToken = useRef<string | null>(null);
    const generation = useRef(0);
    const clearSession = useCallback(async (token?: string) => {
        if (token && token !== currentToken.current) return;
        generation.current++;
        currentToken.current = null;
        setUser(null);
        await AsyncStorage.removeMany(keys);
    }, []);

    const validateSession = useCallback(async () => {
        const revision = generation.current;
        const token = await AsyncStorage.getItem('@Poiesis:token');
        if (revision !== generation.current) return;
        if (!token) { await clearSession(); return; }
        currentToken.current = token;
        try {
            const { data } = await api.get<Usuario>('/v1/auth/session');
            if (revision !== generation.current || currentToken.current !== token) return;
            if (!validSession(data)) { await clearSession(token); return; }
            await AsyncStorage.setItem('@Poiesis:user', JSON.stringify(data));
            setUser(data);
        } catch {
            // Fail closed when the saved session cannot be verified.
            await clearSession(token);
        }
    }, [clearSession]);

    useEffect(() => {
        setUnauthorizedHandler(clearSession);
        void Promise.resolve().then(validateSession).finally(() => setLoading(false));
        const subscription = AppState.addEventListener('change', state => {
            if (state === 'active') void validateSession();
        });
        const check = () => { void validateSession(); };
        if (Platform.OS === 'web') {
            window.addEventListener('pageshow', check);
            window.addEventListener('focus', check);
            window.addEventListener('storage', check);
        }
        return () => {
            setUnauthorizedHandler(undefined);
            subscription.remove();
            if (Platform.OS === 'web') {
                window.removeEventListener('pageshow', check);
                window.removeEventListener('focus', check);
                window.removeEventListener('storage', check);
            }
        };
    }, [clearSession, validateSession]);

    useEffect(() => {
        if (!user) return;
        const timer = setTimeout(() => { void clearSession(); }, Math.min(user.expiresAt - Date.now(), 2147483647));
        return () => clearTimeout(timer);
    }, [user, clearSession]);

    async function signIn(email: string, senha: string) {
        const revision = ++generation.current;
        const response = await api.post<{ token: string }>('/v1/auth/login', { email: email.trim(), senha });
        const token = response.data.token;
        if (!token) throw new Error('O servidor não retornou um token de acesso.');
        const { data } = await api.get<Usuario>('/v1/auth/session', { headers: { Authorization: `Bearer ${token}` } });
        if (!validSession(data)) throw new Error('Sessão inválida ou expirada.');
        if (revision !== generation.current) return;
        await AsyncStorage.setMany({ '@Poiesis:user': JSON.stringify(data), '@Poiesis:token': token });
        currentToken.current = token;
        setUser(data);
    }
    async function signOut() {
        const token = currentToken.current;
        await clearSession();
        if (token) {
            try { await api.post('/v1/auth/logout', undefined, { headers: { Authorization: `Bearer ${token}` } }); }
            catch (error) {
                // An already expired/revoked token requires no further revocation.
                if ((error as { response?: { status: number } }).response?.status !== 401) throw error;
            }
        }
    }
    return <AuthContext.Provider value={{ signed: !!user, isAdmin: user?.roles.includes('ADMIN') ?? false, user, loading, signIn, signOut }}>{children}</AuthContext.Provider>;
};
