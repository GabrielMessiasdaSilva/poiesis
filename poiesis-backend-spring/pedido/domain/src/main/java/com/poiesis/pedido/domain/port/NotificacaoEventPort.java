package com.poiesis.pedido.domain.port;

import com.poiesis.pedido.domain.entity.Pedido;

public interface NotificacaoEventPort {
    void notificarPedidoCriado(Pedido pedido);
}
