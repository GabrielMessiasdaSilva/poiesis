package com.poiesis.customizacao.springframework.repository;

import com.poiesis.customizacao.springframework.repository.entity.CustomizacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataCustomizacaoRepository extends JpaRepository<CustomizacaoEntity, Long> {
    List<CustomizacaoEntity> findByProdutoIdAndAtivoTrue(Long produtoId);
}
