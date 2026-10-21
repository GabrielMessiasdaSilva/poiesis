import { create } from 'axios';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { Platform } from 'react-native';

export const usesSessionCookie = Platform.OS === 'web';
const browserApi = typeof window !== 'undefined'
    ? `${window.location.protocol}//${window.location.hostname}:8080` : 'http://localhost:8080';
const baseURL = process.env.EXPO_PUBLIC_API_URL
    ?? (usesSessionCookie ? browserApi : Platform.OS === 'android' ? 'http://10.0.2.2:8080' : 'http://localhost:8080');
const api = create({ baseURL, timeout: 15000, withCredentials: usesSessionCookie });
const csrfApi = create({ baseURL, timeout: 15000, withCredentials: true });
let csrfRequest: Promise<string> | undefined;
let revision = 0;
let onUnauthorized: ((revision: number) => Promise<void>) | undefined;

export function advanceSessionRevision() { return ++revision; }
export function setUnauthorizedHandler(handler?: (revision: number) => Promise<void>) {
    onUnauthorized = handler;
}

// Compartilha apenas a consulta em andamento. A autenticação pode renovar o cookie
// CSRF no servidor, então cada nova escrita precisa consultar seu valor atual.
// O JWT web nunca é lido pelo JavaScript.
function csrfToken() {
    csrfRequest ??= csrfApi.get<{ token: string }>('/v1/auth/csrf')
        .then(response => response.data.token)
        .finally(() => { csrfRequest = undefined; });
    return csrfRequest;
}

api.interceptors.request.use(async (config) => {
    config.poiesisSessionRevision = revision;
    if (usesSessionCookie) {
        config.headers['X-Auth-Mode'] = 'cookie';
        if (!['get', 'head', 'options'].includes(config.method?.toLowerCase() ?? 'get')) {
            config.headers['X-CSRF-TOKEN'] = await csrfToken();
        }
    } else if (!['/v1/auth/login', '/v1/auth/register'].includes(config.url ?? '')) {
        const token = await AsyncStorage.getItem('@Poiesis:token');
        if (token && !config.headers.Authorization) config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});
api.interceptors.response.use(response => response, async error => {
    const config = error.config;
    const publicRequest = ['/v1/auth/login', '/v1/auth/register'].includes(config?.url ?? '');
    // Uma resposta atrasada não deve encerrar um login mais recente. 403 mantém a sessão.
    if (error.response?.status === 401 && !publicRequest && config?.poiesisSessionRevision === revision) {
        await onUnauthorized?.(revision);
    }
    return Promise.reject(error);
});

declare module 'axios' {
    interface InternalAxiosRequestConfig {
        poiesisSessionRevision?: number;
    }
}

export default api;
