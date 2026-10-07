import { create } from 'axios';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { Platform } from 'react-native';

const api = create({
    baseURL: process.env.EXPO_PUBLIC_API_URL ?? (Platform.OS === 'android' ? 'http://10.0.2.2:8080' : 'http://localhost:8080'),
    timeout: 15000,
});
let onUnauthorized: ((token: string) => Promise<void>) | undefined;
export function setUnauthorizedHandler(handler?: (token: string) => Promise<void>) {
    onUnauthorized = handler;
}
api.interceptors.request.use(async (config) => {
    if (['/v1/auth/login', '/v1/auth/register'].includes(config.url ?? '')) return config;
    const token = await AsyncStorage.getItem('@Poiesis:token');
    if (token && !config.headers.Authorization) config.headers.Authorization = `Bearer ${token}`;
    return config;
});
api.interceptors.response.use(response => response, async error => {
    const authorization = error.config?.headers?.Authorization;
    // A delayed failure from an old session must not end a newer login. 403 keeps the session.
    if (error.response?.status === 401 && typeof authorization === 'string') {
        await onUnauthorized?.(authorization.replace(/^Bearer /, ''));
    }
    return Promise.reject(error);
});
export default api;
