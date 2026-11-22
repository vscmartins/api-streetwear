package com.dt2.streetwear.domain.service;

import com.dt2.streetwear.domain.dto.ProdutoRequest;
import com.dt2.streetwear.domain.dto.ProdutoResponse;
import com.dt2.streetwear.domain.entity.Categoria;
import com.dt2.streetwear.domain.entity.ItemVenda;
import com.dt2.streetwear.domain.entity.Marca;
import com.dt2.streetwear.domain.entity.Produto;
import com.dt2.streetwear.domain.entity.Variacao;
import com.dt2.streetwear.domain.entity.Venda;
import com.dt2.streetwear.domain.exception.RecursoNaoEncontradoException;
import com.dt2.streetwear.domain.repository.ItemVendaRepository;
import com.dt2.streetwear.domain.repository.ProdutoRepository;
import com.dt2.streetwear.domain.repository.VariacaoRepository;
import com.dt2.streetwear.domain.repository.VendaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final MarcaService marcaService;
    private final VariacaoRepository variacaoRepository;
    private final ItemVendaRepository itemVendaRepository;
    private final VendaRepository vendaRepository;

    public ProdutoService(
        ProdutoRepository produtoRepository,
        MarcaService marcaService,
        VariacaoRepository variacaoRepository,
        ItemVendaRepository itemVendaRepository,
        VendaRepository vendaRepository
    ) {
        this.produtoRepository = produtoRepository;
        this.marcaService = marcaService;
        this.variacaoRepository = variacaoRepository;
        this.itemVendaRepository = itemVendaRepository;
        this.vendaRepository = vendaRepository;
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listarComFiltros(Long marcaId, Categoria categoria, String busca) {
        String textoBusca = (busca != null && !busca.trim().isBlank()) ? busca.trim() : null;
        List<Produto> produtos = produtoRepository.buscarComFiltros(marcaId, categoria, textoBusca);
        Map<Long, Integer> estoquePorProduto = carregarEstoqueTotal(produtos);
        return produtos.stream()
            .map(produto -> toResponse(produto, estoquePorProduto.getOrDefault(produto.getId(), 0)))
            .toList();
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscarPorId(Long id) {
        Produto produto = buscarEntidadePorId(id);
        return toResponse(produto, carregarEstoqueTotal(List.of(produto)).getOrDefault(id, 0));
    }

    @Transactional
    public ProdutoResponse salvar(ProdutoRequest request) {
        Marca marca = marcaService.buscarEntidadePorId(request.marcaId());

        Produto produto = new Produto(
            request.nome().trim(),
            request.descricao().trim(),
            request.preco(),
            request.categoria(),
            request.ativo(),
            request.imagemUrl() != null && !request.imagemUrl().trim().isBlank() ? request.imagemUrl().trim() : null,
            marca
        );

        Produto produtoSalvo = produtoRepository.save(produto);
        return toResponse(produtoSalvo, 0);
    }

    @Transactional
    public ProdutoResponse atualizar(Long id, ProdutoRequest request) {
        Produto produto = buscarEntidadePorId(id);
        Marca marca = marcaService.buscarEntidadePorId(request.marcaId());

        produto.setNome(request.nome().trim());
        produto.setDescricao(request.descricao().trim());
        produto.setPreco(request.preco());
        produto.setCategoria(request.categoria());
        produto.setAtivo(request.ativo() != null ? request.ativo() : produto.getAtivo());
        produto.setImagemUrl(request.imagemUrl() != null && !request.imagemUrl().trim().isBlank() ? request.imagemUrl().trim() : null);
        produto.setMarca(marca);

        Produto produtoSalvo = produtoRepository.save(produto);
        return toResponse(produtoSalvo, carregarEstoqueTotal(List.of(produtoSalvo)).getOrDefault(id, 0));
    }

    @Transactional
    public ProdutoResponse atualizarImagem(Long id, String imagemUrl) {
        Produto produto = buscarEntidadePorId(id);
        produto.setImagemUrl(imagemUrl != null && !imagemUrl.trim().isBlank() ? imagemUrl.trim() : null);
        Produto produtoSalvo = produtoRepository.save(produto);
        return toResponse(produtoSalvo, carregarEstoqueTotal(List.of(produtoSalvo)).getOrDefault(id, 0));
    }

    @Transactional
    public void excluir(Long id) {
        Produto produto = buscarEntidadePorId(id);

        List<Variacao> variacoes = variacaoRepository.findByProdutoIdOrderByIdAsc(id);
        List<Long> variacaoIds = variacoes.stream()
            .map(Variacao::getId)
            .toList();
        List<ItemVenda> itensVinculados = variacaoIds.isEmpty()
            ? List.of()
            : itemVendaRepository.findByVariacaoIdIn(variacaoIds);

        Map<Long, Venda> vendasAfetadas = new LinkedHashMap<>();
        for (ItemVenda item : itensVinculados) {
            Venda venda = item.getVenda();
            venda.removerItem(item);
            vendasAfetadas.put(venda.getId(), venda);
        }

        for (Venda venda : vendasAfetadas.values()) {
            BigDecimal novoTotal = venda.getItens().stream()
                .map(item -> item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            venda.setValorTotal(novoTotal);
        }

        vendaRepository.saveAll(vendasAfetadas.values());
        vendaRepository.flush();
        variacaoRepository.deleteAll(variacoes);
        variacaoRepository.flush();
        produtoRepository.delete(produto);
    }

    public Produto buscarEntidadePorId(Long id) {
        return produtoRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Produto nao encontrado com o ID: " + id));
    }

    private Map<Long, Integer> carregarEstoqueTotal(Collection<Produto> produtos) {
        if (produtos.isEmpty()) {
            return Map.of();
        }

        List<Long> produtoIds = produtos.stream()
            .map(Produto::getId)
            .toList();
        return variacaoRepository.findByProdutoIdIn(produtoIds).stream()
            .collect(Collectors.groupingBy(
                variacao -> variacao.getProduto().getId(),
                Collectors.summingInt(Variacao::getEstoque)
            ));
    }

    private ProdutoResponse toResponse(Produto produto, int estoqueTotal) {
        return new ProdutoResponse(
            produto.getId(),
            produto.getNome(),
            produto.getDescricao(),
            produto.getPreco(),
            produto.getCategoria(),
            produto.getAtivo(),
            produto.getImagemUrl(),
            produto.getMarca().getId(),
            produto.getMarca().getNome(),
            estoqueTotal
        );
    }
}
