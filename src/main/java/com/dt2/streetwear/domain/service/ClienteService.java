package com.dt2.streetwear.domain.service;

import com.dt2.streetwear.domain.dto.ClienteRequest;
import com.dt2.streetwear.domain.dto.ClienteResponse;
import com.dt2.streetwear.domain.entity.Cliente;
import com.dt2.streetwear.domain.exception.ConflitoException;
import com.dt2.streetwear.domain.exception.RecursoNaoEncontradoException;
import com.dt2.streetwear.domain.repository.ClienteRepository;
import com.dt2.streetwear.domain.repository.VendaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final VendaRepository vendaRepository;

    public ClienteService(ClienteRepository clienteRepository, VendaRepository vendaRepository) {
        this.clienteRepository = clienteRepository;
        this.vendaRepository = vendaRepository;
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listarTodos() {
        return clienteRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(Long id) {
        Cliente cliente = buscarEntidadePorId(id);
        return toResponse(cliente);
    }

    @Transactional
    public ClienteResponse salvar(ClienteRequest request) {
        String cpfLimpo = request.cpf().replaceAll("\\D", "");
        String emailNormalizado = request.email().trim().toLowerCase();

        if (clienteRepository.existsByCpf(cpfLimpo)) {
            throw new ConflitoException("Ja existe um cliente cadastrado com o CPF: " + cpfLimpo);
        }
        if (clienteRepository.existsByEmailIgnoreCase(emailNormalizado)) {
            throw new ConflitoException("Ja existe um cliente cadastrado com o e-mail: " + emailNormalizado);
        }

        Cliente cliente = new Cliente(
            request.nome().trim(),
            cpfLimpo,
            emailNormalizado,
            request.telefone() != null ? request.telefone().trim() : null,
            request.instagram() != null ? request.instagram().trim() : null
        );

        return toResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteResponse atualizar(Long id, ClienteRequest request) {
        Cliente cliente = buscarEntidadePorId(id);
        String cpfLimpo = request.cpf().replaceAll("\\D", "");
        String emailNormalizado = request.email().trim().toLowerCase();

        if (clienteRepository.existsByCpfAndIdNot(cpfLimpo, id)) {
            throw new ConflitoException("Ja existe outro cliente com o CPF: " + cpfLimpo);
        }
        if (clienteRepository.existsByEmailIgnoreCaseAndIdNot(emailNormalizado, id)) {
            throw new ConflitoException("Ja existe outro cliente com o e-mail: " + emailNormalizado);
        }

        cliente.setNome(request.nome().trim());
        cliente.setCpf(cpfLimpo);
        cliente.setEmail(emailNormalizado);
        cliente.setTelefone(request.telefone() != null ? request.telefone().trim() : null);
        cliente.setInstagram(request.instagram() != null ? request.instagram().trim() : null);

        return toResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public void excluir(Long id) {
        Cliente cliente = buscarEntidadePorId(id);
        if (vendaRepository.existsByClienteId(id)) {
            throw new ConflitoException("Nao e permitido excluir cliente com historico de compras");
        }
        clienteRepository.delete(cliente);
    }

    public Cliente buscarEntidadePorId(Long id) {
        return clienteRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado com o ID: " + id));
    }

    private ClienteResponse toResponse(Cliente cliente) {
        return new ClienteResponse(
            cliente.getId(),
            cliente.getNome(),
            cliente.getCpf(),
            cliente.getEmail(),
            cliente.getTelefone(),
            cliente.getInstagram()
        );
    }
}
