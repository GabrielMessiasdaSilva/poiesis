import React, { createContext, useState, useEffect, useCallback, useRef, ReactNode } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { AppState, Platform } from 'react-native';
import api, { advanceSessionRevision, setUnauthorizedHandler, usesSessionCookie } from '../services/api';

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
    const clearSession = useCallback(async (revision?: number) => {
        if (revision !== undefined && revision !== generation.current) return;
        generation.current = advanceSessionRevision();
        currentToken.current = null;
        setUser(null);
        await AsyncStorage.removeMany(keys);
    }, []);

    const validateSession = useCallback(async () => {
        const revision = generation.current;
        const token = usesSessionCookie ? null : await AsyncStorage.getItem('@Poiesis:token');
        if (revision !== generation.current) return;
        if (!usesSessionCookie && !token) { await clearSession(revision); return; }
        currentToken.current = token;
        try {
            const { data } = await api.get<Usuario>('/v1/auth/session');
            if (revision !== generation.current) return;
            if (!validSession(data)) { await clearSession(revision); return; }
            if (!usesSessionCookie) await AsyncStorage.setItem('@Poiesis:user', JSON.stringify(data));
            setUser(data);
        } catch {
            await clearSession(revision);
        }
    }, [clearSession]);

    useEffect(() => {
        setUnauthorizedHandler(clearSession);
        // Remove credenciais da implementação anterior; a web passa a usar somente o cookie.
        const restore = async () => {
            if (usesSessionCookie) await AsyncStorage.removeMany(keys);
            await validateSession();
        };
        void restore().finally(() => setLoading(false));
        const subscription = AppState.addEventListener('change', state => {
            if (state === 'active') void validateSession();
        });
        const check = () => { void validateSession(); };
        if (Platform.OS === 'web') {
            window.addEventListener('pageshow', check);
            window.addEventListener('focus', check);
        }
        return () => {
            setUnauthorizedHandler(undefined);
            subscription.remove();
            if (Platform.OS === 'web') {
                window.removeEventListener('pageshow', check);
                window.removeEventListener('focus', check);
            }
        };
    }, [clearSession, validateSession]);

    useEffect(() => {
        if (!user) return;
        const revision = generation.current;
        const timer = setTimeout(() => { void clearSession(revision); }, Math.min(user.expiresAt - Date.now(), 2147483647));
        return () => clearTimeout(timer);
    }, [user, clearSession]);

    async function signIn(email: string, senha: string) {
        const revision = generation.current = advanceSessionRevision();
        const response = await api.post<{ token?: string }>('/v1/auth/login', { email: email.trim(), senha });
        const token = usesSessionCookie ? null : response.data.token;
        if (!usesSessionCookie && !token) throw new Error('O servidor não retornou um token de acesso.');
        const { data } = await api.get<Usuario>('/v1/auth/session', token ? { headers: { Authorization: `Bearer ${token}` } } : undefined);
        if (!validSession(data)) throw new Error('Sessão inválida ou expirada.');
        if (revision !== generation.current) return;
        if (!usesSessionCookie && token) {
            await AsyncStorage.setMany({ '@Poiesis:user': JSON.stringify(data), '@Poiesis:token': token });
        }
        currentToken.current = token ?? null;
        setUser(data);
    }
    async function signOut() {
        const revision = generation.current;
        const token = currentToken.current;
        if (usesSessionCookie || token) {
            try { await api.post('/v1/auth/logout', undefined, token ? { headers: { Authorization: `Bearer ${token}` } } : undefined); }
            catch (error) {
                if ((error as { response?: { status: number } }).response?.status !== 401) throw error;
            }
        }
        await clearSession(revision);
    }
    return <AuthContext.Provider value={{ signed: !!user, isAdmin: user?.roles.includes('ADMIN') ?? false, user, loading, signIn, signOut }}>{children}</AuthContext.Provider>;
};
