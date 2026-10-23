package com.poiesis.pedido.config;

import com.poiesis.pedido.integration.CatalogoIntegration;
import com.poiesis.pedido.integration.CustomizacaoIntegration;
import com.poiesis.pedido.messaging.PedidoPublisher;
import com.poiesis.pedido.repository.PedidoRepository;
import com.poiesis.pedido.service.ConsultaPedidoService;
import com.poiesis.pedido.service.PedidoService;
import com.poiesis.pedido.service.SelecionarCustomizacoesService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public PedidoService pedidoService(
            PedidoRepository pedidoRepository,
            CatalogoIntegration catalogoIntegration,
            PedidoPublisher pedidoPublisher,
            CustomizacaoIntegration customizacaoIntegration) {
        return new PedidoService(pedidoRepository, catalogoIntegration, pedidoPublisher, new SelecionarCustomizacoesService(customizacaoIntegration));
    }

    @Bean
    public ConsultaPedidoService consultaPedidoService(PedidoRepository pedidoRepository) {
        return new ConsultaPedidoService(pedidoRepository);
    }
}
