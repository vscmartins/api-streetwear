package com.dt2.streetwear.domain.repository;

import com.dt2.streetwear.domain.entity.Marca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MarcaRepository extends JpaRepository<Marca, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    Optional<Marca> findByNomeIgnoreCase(String nome);
}
