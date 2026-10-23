package com.poiesis.produto;

import com.poiesis.produto.controller.ApiExceptionHandler;
import com.poiesis.produto.controller.CatalogoController;
import com.poiesis.produto.controller.adapter.ProdutoAdapterController;
import com.poiesis.produto.service.CatalogoService;
import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ConcurrentUpdateHttpTests {
    @Test void conflitoDePersistenciaRetorna409ComOrientacao() throws Exception {
        var service = mock(CatalogoService.class);
        doThrow(new OptimisticLockingFailureException("concurrent update")).when(service).inativarProduto(1L);
        var mvc = MockMvcBuilders.standaloneSetup(new CatalogoController(service, new ProdutoAdapterController()))
                .setControllerAdvice(new ApiExceptionHandler()).build();
        mvc.perform(delete("/v1/produtos/1")).andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Atualize a consulta")));
    }
}
