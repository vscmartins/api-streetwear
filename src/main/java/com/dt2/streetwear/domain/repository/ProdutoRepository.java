package com.dt2.streetwear.domain.repository;

import com.dt2.streetwear.domain.entity.Categoria;
import com.dt2.streetwear.domain.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    boolean existsByMarcaId(Long marcaId);

    @Query("""
        SELECT p FROM Produto p
        WHERE (:marcaId IS NULL OR p.marca.id = :marcaId)
          AND (:categoria IS NULL OR p.categoria = :categoria)
          AND (:busca IS NULL OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :busca, '%'))
                             OR LOWER(p.descricao) LIKE LOWER(CONCAT('%', :busca, '%')))
        ORDER BY p.id DESC
    """)
    List<Produto> buscarComFiltros(
        @Param("marcaId") Long marcaId,
        @Param("categoria") Categoria categoria,
        @Param("busca") String busca
    );
}
