import { useCallback, useRef, useState } from 'react';
import { useFocusEffect } from 'expo-router';
import { mensagemApi } from '../services/erros';

// Recarrega ao ganhar foco e descarta respostas de consultas antigas ou de telas já fechadas.
export function useConsulta<T>(consultar: () => Promise<T>, inicial: T, mensagemErro: string) {
    const [dados, setDados] = useState(inicial);
    const [carregando, setCarregando] = useState(true);
    const [erro, setErro] = useState('');
    const revisao = useRef(0);
    const vazio = useRef(inicial);
    const carregar = useCallback(async () => {
        const atual = ++revisao.current;
        setCarregando(true); setErro('');
        try {
            const resposta = await consultar();
            if (atual === revisao.current) setDados(resposta);
        } catch (error: unknown) {
            if (atual === revisao.current) {
                setDados(vazio.current);
                setErro(mensagemApi(error, mensagemErro));
            }
        } finally {
            if (atual === revisao.current) setCarregando(false);
        }
    }, [consultar, mensagemErro]);
    useFocusEffect(useCallback(() => {
        void carregar();
        return () => { revisao.current++; };
    }, [carregar]));
    return { dados, carregando, erro, carregar };
}
