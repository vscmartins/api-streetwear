package com.dt2.streetwear.domain.controller;

import com.dt2.streetwear.domain.dto.MarcaRequest;
import com.dt2.streetwear.domain.dto.MarcaResponse;
import com.dt2.streetwear.domain.service.MarcaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/marcas")
@Tag(name = "Marcas", description = "Operacoes de consulta e cadastro de fabricantes e marcas parceiras")
public class MarcaController {

    private final MarcaService marcaService;

    public MarcaController(MarcaService marcaService) {
        this.marcaService = marcaService;
    }

    @GetMapping
    @Operation(summary = "Listar todas as marcas cadastradas")
    public ResponseEntity<List<MarcaResponse>> listar() {
        return ResponseEntity.ok(marcaService.listarTodas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar marca por ID")
    public ResponseEntity<MarcaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(marcaService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cadastrar uma nova marca")
    public ResponseEntity<MarcaResponse> criar(@Valid @RequestBody MarcaRequest request) {
        MarcaResponse response = marcaService.salvar(request);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.id())
            .toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar dados de uma marca existente")
    public ResponseEntity<MarcaResponse> atualizar(@PathVariable Long id, @Valid @RequestBody MarcaRequest request) {
        return ResponseEntity.ok(marcaService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir uma marca sem produtos vinculados")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        marcaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
