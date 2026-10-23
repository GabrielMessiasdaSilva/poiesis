package com.poiesis.produto.repository.jpa;

import com.poiesis.produto.repository.orm.CustomizacaoOrmJpa;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomizacaoRepositoryWithJpa extends JpaRepository<CustomizacaoOrmJpa, Long> {
    List<CustomizacaoOrmJpa> findByProdutoIdAndAtivoTrue(Long produtoId);
}
