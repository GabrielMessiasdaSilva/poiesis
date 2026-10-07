package com.poiesis.pedido.springframework.controller;

import com.poiesis.pedido.domain.entity.ItemPedido;
import com.poiesis.pedido.domain.entity.Pedido;
import com.poiesis.pedido.domain.usecase.ConsultarPedidosUseCase;
import com.poiesis.pedido.domain.usecase.CriarPedidoUseCase;
import com.poiesis.pedido.springframework.controller.adapter.PedidoMapper;
import com.poiesis.pedido.springframework.controller.dto.request.PedidoRequestDTO;
import com.poiesis.pedido.springframework.controller.dto.response.PedidoResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/pedidos")
public class PedidoController {

    private final CriarPedidoUseCase criarPedidoUseCase;
    private final ConsultarPedidosUseCase consultarPedidosUseCase;
    private final PedidoMapper pedidoMapper;

    public PedidoController(CriarPedidoUseCase criarPedidoUseCase,
                            ConsultarPedidosUseCase consultarPedidosUseCase,
                            PedidoMapper pedidoMapper) {
        this.criarPedidoUseCase = criarPedidoUseCase;
        this.consultarPedidosUseCase = consultarPedidosUseCase;
        this.pedidoMapper = pedidoMapper;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<PedidoResponseDTO> criarPedido(
            @Valid @RequestBody PedidoRequestDTO requestDTO,
            @AuthenticationPrincipal Jwt jwt) {

        // Obtém o e-mail do cliente autenticado via JWT (claims sub ou email)
        String clienteEmail = jwt.getClaimAsString("email");
        if (clienteEmail == null) {
            clienteEmail = jwt.getSubject();
        }

        List<ItemPedido> itensDomain = pedidoMapper.toDomainItemList(requestDTO.getItens());
        Pedido pedidoCriado = criarPedidoUseCase.executar(clienteEmail, itensDomain);

        PedidoResponseDTO responseDTO = pedidoMapper.toResponseDTO(pedidoCriado);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<PedidoResponseDTO> listarPedidos(@AuthenticationPrincipal Jwt jwt) {
        List<Pedido> pedidos = ehAdministrador(jwt)
                ? consultarPedidosUseCase.listarTodos()
                : consultarPedidosUseCase.listarPorClienteEmail(emailDo(jwt));
        return pedidos.stream().map(pedidoMapper::toResponseDTO).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<PedidoResponseDTO> buscarPedido(@PathVariable Long id,
                                                          @AuthenticationPrincipal Jwt jwt) {
        Optional<Pedido> pedidoEncontrado = consultarPedidosUseCase.buscarPorId(id);
        if (pedidoEncontrado.isEmpty()) return ResponseEntity.notFound().build();

        Pedido pedido = pedidoEncontrado.get();
        if (!ehAdministrador(jwt) && !pedido.getClienteEmail().equals(emailDo(jwt))) {
            // Não revela a existência de pedidos de outros clientes.
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(pedidoMapper.toResponseDTO(pedido));
    }

    private boolean ehAdministrador(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        return roles != null && roles.contains("ADMIN");
    }

    private String emailDo(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        return email != null ? email : jwt.getSubject();
    }
}
