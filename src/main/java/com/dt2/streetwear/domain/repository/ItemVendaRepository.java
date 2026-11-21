package com.dt2.streetwear.domain.repository;

import com.dt2.streetwear.domain.entity.ItemVenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemVendaRepository extends JpaRepository<ItemVenda, Long> {

    boolean existsByVariacaoId(Long variacaoId);

    List<ItemVenda> findByVariacaoIdIn(List<Long> variacaoIds);

    List<ItemVenda> findByVendaIdOrderByIdAsc(Long vendaId);
}
