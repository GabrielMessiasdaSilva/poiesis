package com.poiesis.pedido.controller;

import com.poiesis.pedido.entity.ItemPedido;
import com.poiesis.pedido.entity.Pedido;
import com.poiesis.pedido.service.ConsultaPedidoService;
import com.poiesis.pedido.service.PedidoService;
import com.poiesis.pedido.controller.adapter.PedidoAdapterController;
import com.poiesis.pedido.controller.dto.request.PedidoRequest;
import com.poiesis.pedido.controller.dto.response.PedidoResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final ConsultaPedidoService consultaPedidoService;
    private final PedidoAdapterController pedidoAdapter;

    public PedidoController(PedidoService pedidoService,
                            ConsultaPedidoService consultaPedidoService,
                            PedidoAdapterController pedidoAdapter) {
        this.pedidoService = pedidoService;
        this.consultaPedidoService = consultaPedidoService;
        this.pedidoAdapter = pedidoAdapter;
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> criarPedido(
            @Valid @RequestBody PedidoRequest requestDTO,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Roles") List<String> roles) {

        // Contas administrativas que também possuem USER não podem usar o fluxo de compra.
        if (roles.contains("ADMIN")) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        // Identidade autenticada e sobrescrita pelo gateway.
        List<ItemPedido> itensDomain = pedidoAdapter.toDomainItemList(requestDTO.getItens());
        Pedido pedidoCriado = pedidoService.criarPedido(email, itensDomain);

        PedidoResponse responseDTO = pedidoAdapter.toResponse(pedidoCriado);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping
    public List<PedidoResponse> listarPedidos(@RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Roles") List<String> roles) {
        List<Pedido> pedidos = consultaPedidoService.listarParaCliente(email, roles.contains("ADMIN"));
        return pedidos.stream().map(pedidoAdapter::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> buscarPedido(@PathVariable Long id,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Roles") List<String> roles) {
        return consultaPedidoService.buscarParaCliente(id, email, roles.contains("ADMIN"))
                .map(pedidoAdapter::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
