package com.dt2.streetwear.domain.repository;

import com.dt2.streetwear.domain.entity.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendaRepository extends JpaRepository<Venda, Long> {

    boolean existsByClienteId(Long clienteId);

    @Query("SELECT v FROM Venda v JOIN FETCH v.cliente WHERE v.cliente.id = :clienteId ORDER BY v.dataVenda DESC")
    List<Venda> findByClienteIdOrderByDataVendaDesc(@Param("clienteId") Long clienteId);

    @Query("SELECT DISTINCT v FROM Venda v JOIN FETCH v.cliente ORDER BY v.dataVenda DESC")
    List<Venda> findAllWithClienteOrderByDataVendaDesc();
}
