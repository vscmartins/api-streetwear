package com.dt2.streetwear.domain.controller;

import com.dt2.streetwear.domain.dto.AtualizarImagemRequest;
import com.dt2.streetwear.domain.dto.ProdutoRequest;
import com.dt2.streetwear.domain.dto.ProdutoResponse;
import com.dt2.streetwear.domain.entity.Categoria;
import com.dt2.streetwear.domain.service.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/produtos")
@Tag(name = "Produtos", description = "Operacoes de catalogo de produtos com busca e filtros combinaveis")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping
    @Operation(summary = "Listar produtos com filtros combinaveis por marca, categoria e termo de busca")
    public ResponseEntity<List<ProdutoResponse>> listar(
        @RequestParam(required = false) Long marca,
        @RequestParam(required = false) Categoria categoria,
        @RequestParam(required = false) String busca
    ) {
        return ResponseEntity.ok(produtoService.listarComFiltros(marca, categoria, busca));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar produto por ID")
    public ResponseEntity<ProdutoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(produtoService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cadastrar um novo produto")
    public ResponseEntity<ProdutoResponse> criar(@Valid @RequestBody ProdutoRequest request) {
        ProdutoResponse response = produtoService.salvar(request);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.id())
            .toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar informacoes do produto")
    public ResponseEntity<ProdutoResponse> atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoRequest request) {
        return ResponseEntity.ok(produtoService.atualizar(id, request));
    }

    @PatchMapping("/{id}/imagem")
    @Operation(summary = "Atualizar somente a URL da imagem de um produto")
    public ResponseEntity<ProdutoResponse> atualizarImagem(@PathVariable Long id, @Valid @RequestBody AtualizarImagemRequest request) {
        return ResponseEntity.ok(produtoService.atualizarImagem(id, request.imagemUrl()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir um produto sem variacoes vinculadas")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        produtoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
