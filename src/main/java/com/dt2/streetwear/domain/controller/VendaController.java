package com.dt2.streetwear.domain.controller;

import com.dt2.streetwear.domain.dto.*;
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
@RequestMapping("/api/vendas")
@Tag(name = "Vendas", description = "Criacao transacional de pedidos, baixa de estoque e gerenciamento de status")
public class VendaController {

    private final VendaService vendaService;

    public VendaController(VendaService vendaService) {
        this.vendaService = vendaService;
    }

    @GetMapping
    @Operation(summary = "Listar todas as vendas registradas com clientes e itens")
    public ResponseEntity<List<VendaResponse>> listar() {
        return ResponseEntity.ok(vendaService.listarTodas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar detalhes de uma venda por ID")
    public ResponseEntity<VendaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(vendaService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Criar nova venda com baixa atomica de estoque e calculo de valor total")
    public ResponseEntity<VendaResponse> criar(@Valid @RequestBody VendaRequest request) {
        VendaResponse response = vendaService.criar(request);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.id())
            .toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status da venda (PENDENTE -> PAGA -> ENVIADA -> ENTREGUE / CANCELADA)")
    public ResponseEntity<VendaResponse> atualizarStatus(
        @PathVariable Long id,
        @Valid @RequestBody AtualizarStatusRequest request
    ) {
        return ResponseEntity.ok(vendaService.atualizarStatus(id, request));
    }

    @PostMapping("/{id}/itens")
    @Operation(summary = "Adicionar um item a uma venda PENDENTE com ajuste imediato de estoque")
    public ResponseEntity<VendaResponse> adicionarItem(
        @PathVariable Long id,
        @Valid @RequestBody ItemVendaRequest request
    ) {
        return ResponseEntity.ok(vendaService.adicionarItem(id, request));
    }

    @PutMapping("/{vendaId}/itens/{itemId}")
    @Operation(summary = "Atualizar quantidade de um item em venda PENDENTE")
    public ResponseEntity<VendaResponse> atualizarItem(
        @PathVariable Long vendaId,
        @PathVariable Long itemId,
        @Valid @RequestBody AtualizarItemRequest request
    ) {
        return ResponseEntity.ok(vendaService.atualizarItem(vendaId, itemId, request));
    }

    @DeleteMapping("/{vendaId}/itens/{itemId}")
    @Operation(summary = "Remover um item de venda PENDENTE devolvendo seu saldo ao estoque")
    public ResponseEntity<VendaResponse> removerItem(
        @PathVariable Long vendaId,
        @PathVariable Long itemId
    ) {
        return ResponseEntity.ok(vendaService.removerItem(vendaId, itemId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir uma venda (devolve itens ao estoque se nao entregue)")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        vendaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
