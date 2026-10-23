package com.poiesis.produto.repository.jpa;

import com.poiesis.produto.entity.Categoria;
import com.poiesis.produto.repository.orm.ProdutoOrmJpa;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProdutoRepositoryWithJpa extends JpaRepository<ProdutoOrmJpa, Long> {
    List<ProdutoOrmJpa> findByAtivoTrue();
    List<ProdutoOrmJpa> findByCategoriaAndAtivoTrue(Categoria categoria);
}