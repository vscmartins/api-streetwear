package com.dt2.streetwear.domain.service;

import com.dt2.streetwear.domain.dto.VariacaoRequest;
import com.dt2.streetwear.domain.dto.VariacaoResponse;
import com.dt2.streetwear.domain.entity.Produto;
import com.dt2.streetwear.domain.entity.Variacao;
import com.dt2.streetwear.domain.exception.ConflitoException;
import com.dt2.streetwear.domain.exception.RecursoNaoEncontradoException;
import com.dt2.streetwear.domain.repository.ItemVendaRepository;
import com.dt2.streetwear.domain.repository.VariacaoRepository;
import com.dt2.streetwear.domain.service.ProdutoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VariacaoService {

    private final VariacaoRepository variacaoRepository;
    private final ProdutoService produtoService;
    private final ItemVendaRepository itemVendaRepository;

    public VariacaoService(
        VariacaoRepository variacaoRepository,
        ProdutoService produtoService,
        ItemVendaRepository itemVendaRepository
    ) {
        this.variacaoRepository = variacaoRepository;
        this.produtoService = produtoService;
        this.itemVendaRepository = itemVendaRepository;
    }

    @Transactional(readOnly = true)
    public List<VariacaoResponse> listarTodas() {
        return variacaoRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<VariacaoResponse> listarPorProduto(Long produtoId) {
        produtoService.buscarEntidadePorId(produtoId);
        return variacaoRepository.findByProdutoIdOrderByIdAsc(produtoId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<VariacaoResponse> buscarEstoqueBaixo() {
        return variacaoRepository.buscarEstoqueBaixo().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public VariacaoResponse buscarPorId(Long id) {
        Variacao variacao = buscarEntidadePorId(id);
        return toResponse(variacao);
    }

    @Transactional
    public VariacaoResponse salvar(VariacaoRequest request) {
        Produto produto = produtoService.buscarEntidadePorId(request.produtoId());
        String skuNormalizado = request.sku().trim().toUpperCase();
        String tamanhoNormalizado = request.tamanho().trim().toUpperCase();
        String corNormalizada = request.cor().trim();

        if (variacaoRepository.existsBySkuIgnoreCase(skuNormalizado)) {
            throw new ConflitoException("Ja existe uma variacao com o SKU: " + skuNormalizado);
        }

        if (variacaoRepository.existsByProdutoIdAndTamanhoIgnoreCaseAndCorIgnoreCase(
            produto.getId(), tamanhoNormalizado, corNormalizada)) {
            throw new ConflitoException(String.format(
                "Ja existe variacao cadastrada para o produto '%s' com tamanho '%s' e cor '%s'",
                produto.getNome(), tamanhoNormalizado, corNormalizada
            ));
        }

        Variacao variacao = new Variacao(
            skuNormalizado,
            tamanhoNormalizado,
            corNormalizada,
            request.estoque(),
            request.estoqueMinimo(),
            produto
        );

        return toResponse(variacaoRepository.save(variacao));
    }

    @Transactional
    public VariacaoResponse atualizar(Long id, VariacaoRequest request) {
        Variacao variacao = buscarEntidadePorId(id);
        Produto produto = produtoService.buscarEntidadePorId(request.produtoId());
        String skuNormalizado = request.sku().trim().toUpperCase();
        String tamanhoNormalizado = request.tamanho().trim().toUpperCase();
        String corNormalizada = request.cor().trim();

        if (variacaoRepository.existsBySkuIgnoreCaseAndIdNot(skuNormalizado, id)) {
            throw new ConflitoException("Ja existe outra variacao cadastrada com o SKU: " + skuNormalizado);
        }

        if (variacaoRepository.existsByProdutoIdAndTamanhoIgnoreCaseAndCorIgnoreCaseAndIdNot(
            produto.getId(), tamanhoNormalizado, corNormalizada, id)) {
            throw new ConflitoException(String.format(
                "Ja existe outra variacao para o produto '%s' com tamanho '%s' e cor '%s'",
                produto.getNome(), tamanhoNormalizado, corNormalizada
            ));
        }

        variacao.setSku(skuNormalizado);
        variacao.setTamanho(tamanhoNormalizado);
        variacao.setCor(corNormalizada);
        variacao.setEstoque(request.estoque());
        variacao.setEstoqueMinimo(request.estoqueMinimo());
        variacao.setProduto(produto);

        return toResponse(variacaoRepository.save(variacao));
    }

    @Transactional
    public void excluir(Long id) {
        Variacao variacao = buscarEntidadePorId(id);
        if (itemVendaRepository.existsByVariacaoId(id)) {
            throw new ConflitoException("Nao e permitido excluir variacao presente em historico de vendas");
        }
        variacaoRepository.delete(variacao);
    }

    public Variacao buscarEntidadePorId(Long id) {
        return variacaoRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Variacao nao encontrada com o ID: " + id));
    }

    public VariacaoResponse toResponse(Variacao variacao) {
        return new VariacaoResponse(
            variacao.getId(),
            variacao.getSku(),
            variacao.getTamanho(),
            variacao.getCor(),
            variacao.getEstoque(),
            variacao.getEstoqueMinimo(),
            variacao.getProduto().getId(),
            variacao.getProduto().getNome()
        );
    }
}
