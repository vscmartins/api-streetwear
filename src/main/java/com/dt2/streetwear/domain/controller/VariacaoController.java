package com.dt2.streetwear.domain.controller;

import com.dt2.streetwear.domain.dto.VariacaoRequest;
import com.dt2.streetwear.domain.dto.VariacaoResponse;
import com.dt2.streetwear.domain.service.VariacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/variacoes")
@Tag(name = "Variacoes de Estoque", description = "Gestao de SKUs especificos por tamanho, cor e monitoramento de estoque")
public class VariacaoController {

    private final VariacaoService variacaoService;

    public VariacaoController(VariacaoService variacaoService) {
        this.variacaoService = variacaoService;
    }

    @GetMapping
    @Operation(summary = "Listar todas as variacoes cadastradas no estoque")
    public ResponseEntity<List<VariacaoResponse>> listar() {
        return ResponseEntity.ok(variacaoService.listarTodas());
    }

    @GetMapping("/produto/{produtoId}")
    @Operation(summary = "Listar variacoes de tamanho e cor de um produto especifico")
    public ResponseEntity<List<VariacaoResponse>> listarPorProduto(@PathVariable Long produtoId) {
        return ResponseEntity.ok(variacaoService.listarPorProduto(produtoId));
    }

    @GetMapping("/estoque-baixo")
    @Operation(summary = "Listar variacoes cujo saldo em estoque esta menor ou igual ao estoque minimo")
    public ResponseEntity<List<VariacaoResponse>> buscarEstoqueBaixo() {
        return ResponseEntity.ok(variacaoService.buscarEstoqueBaixo());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar variacao por ID")
    public ResponseEntity<VariacaoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(variacaoService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cadastrar uma nova variacao/SKU para um produto")
    public ResponseEntity<VariacaoResponse> criar(@Valid @RequestBody VariacaoRequest request) {
        VariacaoResponse response = variacaoService.salvar(request);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.id())
            .toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar dados de uma variacao/SKU existente")
    public ResponseEntity<VariacaoResponse> atualizar(@PathVariable Long id, @Valid @RequestBody VariacaoRequest request) {
        return ResponseEntity.ok(variacaoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir uma variacao de estoque sem historico de vendas")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        variacaoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
