package com.dt2.streetwear.domain.repository;

import com.dt2.streetwear.domain.entity.Variacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface VariacaoRepository extends JpaRepository<Variacao, Long> {

    boolean existsByProdutoId(Long produtoId);

    boolean existsBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCaseAndIdNot(String sku, Long id);

    boolean existsByProdutoIdAndTamanhoIgnoreCaseAndCorIgnoreCase(Long produtoId, String tamanho, String cor);

    boolean existsByProdutoIdAndTamanhoIgnoreCaseAndCorIgnoreCaseAndIdNot(Long produtoId, String tamanho, String cor, Long id);

    List<Variacao> findByProdutoIdOrderByIdAsc(Long produtoId);

    List<Variacao> findByProdutoIdIn(Collection<Long> produtoIds);

    Optional<Variacao> findBySkuIgnoreCase(String sku);

    @Query("SELECT v FROM Variacao v WHERE v.estoque <= v.estoqueMinimo ORDER BY v.estoque ASC")
    List<Variacao> buscarEstoqueBaixo();
}
