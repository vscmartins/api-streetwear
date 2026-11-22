package com.dt2.streetwear.domain.controller;

import com.dt2.streetwear.domain.dto.ClienteRequest;
import com.dt2.streetwear.domain.dto.ClienteResponse;
import com.dt2.streetwear.domain.dto.VendaResponse;
import com.dt2.streetwear.domain.service.ClienteService;
import com.dt2.streetwear.domain.service.VendaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "Cadastro de compradores e historico de pedidos")
public class ClienteController {

    private final ClienteService clienteService;
    private final VendaService vendaService;

    public ClienteController(ClienteService clienteService, VendaService vendaService) {
        this.clienteService = clienteService;
        this.vendaService = vendaService;
    }

    @GetMapping
    @Operation(summary = "Listar todos os clientes cadastrados")
    public ResponseEntity<List<ClienteResponse>> listar() {
        return ResponseEntity.ok(clienteService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por ID")
    public ResponseEntity<ClienteResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }

    @GetMapping("/{id}/vendas")
    @Operation(summary = "Listar historico completo de compras realizadas por um cliente")
    public ResponseEntity<List<VendaResponse>> listarHistoricoVendas(@PathVariable Long id) {
        return ResponseEntity.ok(vendaService.listarPorCliente(id));
    }

    @PostMapping
    @Operation(summary = "Cadastrar um novo cliente")
    public ResponseEntity<ClienteResponse> criar(@Valid @RequestBody ClienteRequest request) {
        ClienteResponse response = clienteService.salvar(request);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.id())
            .toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar informacoes cadastrais do cliente")
    public ResponseEntity<ClienteResponse> atualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.ok(clienteService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir um cliente sem historico de compras")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        clienteService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
