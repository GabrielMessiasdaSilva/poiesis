export const artesBase = [
    { id: 'flor-geometrica', nome: 'Flor geométrica', descricao: 'Traço orgânico para o centro da peça.', arquivo: 'assets/customization/flor-geometrica.svg' },
    { id: 'sol-minimalista', nome: 'Sol minimalista', descricao: 'Símbolo leve em linha contínua.', arquivo: 'assets/customization/sol-minimalista.svg' },
] as const;

export function referenciaArteBase(id: string) {
    return `asset://poiesis/${id}`;
}