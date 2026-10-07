package com.poiesis.pedido.springframework.config;

import com.poiesis.pedido.domain.repository.PedidoRepositoryPort;
import com.poiesis.pedido.domain.port.CatalogoServicePort;
import com.poiesis.pedido.domain.port.NotificacaoEventPort;
import com.poiesis.pedido.domain.usecase.ConsultarPedidosUseCase;
import com.poiesis.pedido.domain.usecase.CriarPedidoUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public CriarPedidoUseCase criarPedidoUseCase(
            PedidoRepositoryPort pedidoRepositoryPort,
            CatalogoServicePort catalogoServicePort,
            NotificacaoEventPort notificacaoEventPort,
            com.poiesis.pedido.domain.port.CustomizacaoServicePort customizacaoServicePort) {
        return new CriarPedidoUseCase(pedidoRepositoryPort, catalogoServicePort, notificacaoEventPort, customizacaoServicePort);
    }

    @Bean
    public ConsultarPedidosUseCase consultarPedidosUseCase(PedidoRepositoryPort pedidoRepositoryPort) {
        return new ConsultarPedidosUseCase(pedidoRepositoryPort);
    }
}
