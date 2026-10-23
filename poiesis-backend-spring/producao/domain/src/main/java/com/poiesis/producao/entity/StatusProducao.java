package com.poiesis.producao.entity;

public enum StatusProducao {
    PENDENTE,
    APROVADO,
    EM_CORTE,
    EM_COSTURA,
    EM_PERSONALIZACAO,
    FINALIZADO,
    ENTREGUE,
    /** @deprecated Use EM_PERSONALIZACAO. Mantido para dados legados. */
    @Deprecated
    ACABAMENTO,
    /** @deprecated Use FINALIZADO. Mantido para dados legados. */
    @Deprecated
    CONCLUIDO,
    CANCELADO;

    public boolean podeTransicionarPara(StatusProducao destino) {
        if (destino == null || destino == this) return false;
        if (destino == CANCELADO) return this != ENTREGUE && this != FINALIZADO;
        return switch (this) {
            case PENDENTE -> destino == APROVADO;
            case APROVADO -> destino == EM_CORTE;
            case EM_CORTE -> destino == EM_COSTURA;
            case EM_COSTURA -> destino == EM_PERSONALIZACAO;
            case EM_PERSONALIZACAO, ACABAMENTO -> destino == FINALIZADO;
            case FINALIZADO, CONCLUIDO -> destino == ENTREGUE;
            case ENTREGUE, CANCELADO -> false;
        };
    }
}   