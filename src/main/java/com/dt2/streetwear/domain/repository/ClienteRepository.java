package com.dt2.streetwear.domain.repository;

import com.dt2.streetwear.domain.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    Optional<Cliente> findByCpf(String cpf);

    Optional<Cliente> findByEmailIgnoreCase(String email);
}
