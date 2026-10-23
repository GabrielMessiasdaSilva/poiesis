package com.poiesis.pedido.messaging;

import com.poiesis.pedido.entity.Pedido;

public interface PedidoPublisher {
    void notificarPedidoCriado(Pedido pedido);
}
