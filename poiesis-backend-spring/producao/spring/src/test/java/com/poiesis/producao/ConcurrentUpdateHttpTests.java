package com.poiesis.producao;

import com.poiesis.producao.controller.ApiExceptionHandler;
import com.poiesis.producao.controller.ProducaoController;
import com.poiesis.producao.controller.adapter.OrdemProducaoAdapterController;
import com.poiesis.producao.entity.StatusProducao;
import com.poiesis.producao.service.ProducaoService;
import com.poiesis.producao.service.ResumoProducaoService;
import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ConcurrentUpdateHttpTests {
    @Test void conflitoAoMudarEtapaRetorna409() throws Exception {
        var service = mock(ProducaoService.class);
        when(service.atualizarStatus(1L, StatusProducao.EM_CORTE))
                .thenThrow(new OptimisticLockingFailureException("concurrent update"));
        var mvc = MockMvcBuilders.standaloneSetup(new ProducaoController(service,
                new OrdemProducaoAdapterController(), mock(ResumoProducaoService.class)))
                .setControllerAdvice(new ApiExceptionHandler()).build();
        mvc.perform(patch("/v1/producao/1/status").contentType("application/json").content("{\"status\":\"EM_CORTE\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Atualize a consulta")));
    }
}
