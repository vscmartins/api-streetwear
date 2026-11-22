package com.dt2.streetwear.domain.service;

import com.dt2.streetwear.domain.dto.MarcaRequest;
import com.dt2.streetwear.domain.dto.MarcaResponse;
import com.dt2.streetwear.domain.entity.Marca;
import com.dt2.streetwear.domain.exception.ConflitoException;
import com.dt2.streetwear.domain.exception.RecursoNaoEncontradoException;
import com.dt2.streetwear.domain.repository.MarcaRepository;
import com.dt2.streetwear.domain.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MarcaService {

    private final MarcaRepository marcaRepository;
    private final ProdutoRepository produtoRepository;

    public MarcaService(MarcaRepository marcaRepository, ProdutoRepository produtoRepository) {
        this.marcaRepository = marcaRepository;
        this.produtoRepository = produtoRepository;
    }

    @Transactional(readOnly = true)
    public List<MarcaResponse> listarTodas() {
        return marcaRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public MarcaResponse buscarPorId(Long id) {
        Marca marca = buscarEntidadePorId(id);
        return toResponse(marca);
    }

    @Transactional
    public MarcaResponse salvar(MarcaRequest request) {
        String nomeNormalizado = request.nome().trim();
        if (marcaRepository.existsByNomeIgnoreCase(nomeNormalizado)) {
            throw new ConflitoException("Ja existe uma marca cadastrada com o nome: " + nomeNormalizado);
        }

        Marca marca = new Marca(
            nomeNormalizado,
            request.paisOrigem() != null ? request.paisOrigem().trim() : null,
            request.descricao() != null ? request.descricao().trim() : null
        );

        return toResponse(marcaRepository.save(marca));
    }

    @Transactional
    public MarcaResponse atualizar(Long id, MarcaRequest request) {
        Marca marca = buscarEntidadePorId(id);
        String nomeNormalizado = request.nome().trim();

        if (marcaRepository.existsByNomeIgnoreCaseAndIdNot(nomeNormalizado, id)) {
            throw new ConflitoException("Ja existe outra marca cadastrada com o nome: " + nomeNormalizado);
        }

        marca.setNome(nomeNormalizado);
        marca.setPaisOrigem(request.paisOrigem() != null ? request.paisOrigem().trim() : null);
        marca.setDescricao(request.descricao() != null ? request.descricao().trim() : null);

        return toResponse(marcaRepository.save(marca));
    }

    @Transactional
    public void excluir(Long id) {
        Marca marca = buscarEntidadePorId(id);
        if (produtoRepository.existsByMarcaId(id)) {
            throw new ConflitoException("Nao e permitido excluir marca vinculada a produtos cadastrados");
        }
        marcaRepository.delete(marca);
    }

    public Marca buscarEntidadePorId(Long id) {
        return marcaRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Marca nao encontrada com o ID: " + id));
    }

    private MarcaResponse toResponse(Marca marca) {
        return new MarcaResponse(
            marca.getId(),
            marca.getNome(),
            marca.getPaisOrigem(),
            marca.getDescricao()
        );
    }
}
