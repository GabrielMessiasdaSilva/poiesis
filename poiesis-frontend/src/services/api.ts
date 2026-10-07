import { create } from 'axios';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { Platform } from 'react-native';

const defaultBaseUrl = Platform.OS === 'android'
    ? 'http://10.0.2.2:8080'
    : 'http://localhost:8080';

const api = create({
    baseURL: process.env.EXPO_PUBLIC_API_URL ?? defaultBaseUrl,
});

api.interceptors.request.use(async (config) => {
    const token = await AsyncStorage.getItem('@Poiesis:token');
    if (token && !token.startsWith('token-falso-')) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export default api;
