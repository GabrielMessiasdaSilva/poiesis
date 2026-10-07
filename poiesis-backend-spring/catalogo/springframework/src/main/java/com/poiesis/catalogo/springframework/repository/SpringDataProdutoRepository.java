package com.poiesis.catalogo.springframework.repository;

import com.poiesis.catalogo.domain.entity.Categoria;
import com.poiesis.catalogo.springframework.repository.entity.ProdutoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataProdutoRepository extends JpaRepository<ProdutoEntity, Long> {
    List<ProdutoEntity> findByAtivoTrue();
    List<ProdutoEntity> findByCategoriaAndAtivoTrue(Categoria categoria);
}