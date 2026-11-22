package com.dt2.streetwear.domain.service;

import com.dt2.streetwear.domain.dto.*;
import com.dt2.streetwear.domain.entity.*;
import com.dt2.streetwear.domain.exception.ConflitoException;
import com.dt2.streetwear.domain.exception.RecursoNaoEncontradoException;
import com.dt2.streetwear.domain.exception.RegraNegocioException;
import com.dt2.streetwear.domain.repository.ItemVendaRepository;
import com.dt2.streetwear.domain.repository.VendaRepository;
import com.dt2.streetwear.domain.service.ClienteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VendaService {

    private final VendaRepository vendaRepository;
    private final ItemVendaRepository itemVendaRepository;
    private final ClienteService clienteService;
    private final VariacaoService variacaoService;

    public VendaService(
        VendaRepository vendaRepository,
        ItemVendaRepository itemVendaRepository,
        ClienteService clienteService,
        VariacaoService variacaoService
    ) {
        this.vendaRepository = vendaRepository;
        this.itemVendaRepository = itemVendaRepository;
        this.clienteService = clienteService;
        this.variacaoService = variacaoService;
    }

    @Transactional(readOnly = true)
    public List<VendaResponse> listarTodas() {
        return vendaRepository.findAllWithClienteOrderByDataVendaDesc().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<VendaResponse> listarPorCliente(Long clienteId) {
        clienteService.buscarEntidadePorId(clienteId);
        return vendaRepository.findByClienteIdOrderByDataVendaDesc(clienteId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public VendaResponse buscarPorId(Long id) {
        Venda venda = buscarEntidadePorId(id);
        return toResponse(venda);
    }

    /*
     * Realiza a criacao transacional da venda: valida cliente, disponibilidade de estoque,
     * status ativo do produto, congela o preco unitario e debita o saldo do estoque atomicamente.
     */
    @Transactional
    public VendaResponse criar(VendaRequest request) {
        Cliente cliente = clienteService.buscarEntidadePorId(request.clienteId());

        Venda venda = new Venda(
            cliente,
            LocalDateTime.now(),
            request.formaPagamento(),
            StatusVenda.PENDENTE
        );

        BigDecimal valorTotal = BigDecimal.ZERO;

        for (ItemVendaRequest itemReq : request.itens()) {
            Variacao variacao = variacaoService.buscarEntidadePorId(itemReq.variacaoId());
            Produto produto = variacao.getProduto();

            if (!Boolean.TRUE.equals(produto.getAtivo())) {
                throw new RegraNegocioException(String.format(
                    "O produto '%s' esta inativo e nao pode ser comercializado", produto.getNome()));
            }

            if (variacao.getEstoque() < itemReq.quantidade()) {
                throw new ConflitoException(String.format(
                    "Estoque insuficiente para o SKU %s: disponivel %d, solicitado %d",
                    variacao.getSku(), variacao.getEstoque(), itemReq.quantidade()
                ));
            }

            // Baixa no estoque fisico
            variacao.setEstoque(variacao.getEstoque() - itemReq.quantidade());

            // Preco unitario congelado a partir do preco cadastrado no momento da venda
            BigDecimal precoUnitario = produto.getPreco();
            ItemVenda item = new ItemVenda(venda, variacao, itemReq.quantidade(), precoUnitario);
            venda.adicionarItem(item);

            BigDecimal subtotal = precoUnitario.multiply(BigDecimal.valueOf(itemReq.quantidade()));
            valorTotal = valorTotal.add(subtotal);
        }

        venda.setValorTotal(valorTotal);
        return toResponse(vendaRepository.save(venda));
    }

    /*
     * Maquina de estados finita da Venda usando Pattern Matching / Switch Expressions do Java 21.
     * Fluxo permitido: PENDENTE -> PAGA -> ENVIADA -> ENTREGUE. Cancelamento permitido a partir de
     * PENDENTE ou PAGA, acionando a devolucao automatica dos itens ao estoque.
     */
    @Transactional
    public VendaResponse atualizarStatus(Long id, AtualizarStatusRequest request) {
        Venda venda = buscarEntidadePorId(id);
        StatusVenda statusAtual = venda.getStatus();
        StatusVenda novoStatus = request.status();

        if (statusAtual == novoStatus) {
            return toResponse(venda);
        }

        boolean transicaoValida = switch (statusAtual) {
            case PENDENTE -> (novoStatus == StatusVenda.PAGA || novoStatus == StatusVenda.CANCELADA);
            case PAGA -> (novoStatus == StatusVenda.ENVIADA || novoStatus == StatusVenda.CANCELADA);
            case ENVIADA -> (novoStatus == StatusVenda.ENTREGUE);
            case ENTREGUE, CANCELADA -> false; // Estados finais imutaveis
        };

        if (!transicaoValida) {
            throw new ConflitoException(String.format(
                "Transicao de status invalida de '%s' para '%s'", statusAtual, novoStatus));
        }

        // Devolucao automatica de saldo ao estoque quando a venda for cancelada
        if (novoStatus == StatusVenda.CANCELADA) {
            for (ItemVenda item : venda.getItens()) {
                Variacao variacao = item.getVariacao();
                variacao.setEstoque(variacao.getEstoque() + item.getQuantidade());
            }
        }

        venda.setStatus(novoStatus);
        return toResponse(vendaRepository.save(venda));
    }

    @Transactional
    public VendaResponse adicionarItem(Long vendaId, ItemVendaRequest itemReq) {
        Venda venda = buscarEntidadePorId(vendaId);
        validarVendaPendente(venda, "adicionar itens a");

        Variacao variacao = variacaoService.buscarEntidadePorId(itemReq.variacaoId());
        Produto produto = variacao.getProduto();

        if (!Boolean.TRUE.equals(produto.getAtivo())) {
            throw new RegraNegocioException(String.format(
                "O produto '%s' esta inativo e nao pode ser comercializado", produto.getNome()));
        }

        if (variacao.getEstoque() < itemReq.quantidade()) {
            throw new ConflitoException(String.format(
                "Estoque insuficiente para o SKU %s: disponivel %d, solicitado %d",
                variacao.getSku(), variacao.getEstoque(), itemReq.quantidade()
            ));
        }

        variacao.setEstoque(variacao.getEstoque() - itemReq.quantidade());
        ItemVenda novoItem = new ItemVenda(venda, variacao, itemReq.quantidade(), produto.getPreco());
        venda.adicionarItem(novoItem);

        recalcularValorTotal(venda);
        return toResponse(vendaRepository.save(venda));
    }

    @Transactional
    public VendaResponse atualizarItem(Long vendaId, Long itemId, AtualizarItemRequest request) {
        Venda venda = buscarEntidadePorId(vendaId);
        validarVendaPendente(venda, "modificar itens de");

        ItemVenda item = itemVendaRepository.findById(itemId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Item nao encontrado com o ID: " + itemId));

        if (!item.getVenda().getId().equals(vendaId)) {
            throw new RegraNegocioException("O item informado nao pertence a esta venda");
        }

        int quantidadeAntiga = item.getQuantidade();
        int novaQuantidade = request.quantidade();
        int diferenca = novaQuantidade - quantidadeAntiga;

        Variacao variacao = item.getVariacao();

        if (diferenca > 0) {
            if (variacao.getEstoque() < diferenca) {
                throw new ConflitoException(String.format(
                    "Estoque insuficiente para o SKU %s: disponivel %d, solicitado adicional %d",
                    variacao.getSku(), variacao.getEstoque(), diferenca
                ));
            }
            variacao.setEstoque(variacao.getEstoque() - diferenca);
        } else if (diferenca < 0) {
            variacao.setEstoque(variacao.getEstoque() + (-diferenca));
        }

        item.setQuantidade(novaQuantidade);
        recalcularValorTotal(venda);
        return toResponse(vendaRepository.save(venda));
    }

    @Transactional
    public VendaResponse removerItem(Long vendaId, Long itemId) {
        Venda venda = buscarEntidadePorId(vendaId);
        validarVendaPendente(venda, "remover itens de");

        if (venda.getItens().size() <= 1) {
            throw new RegraNegocioException("A venda deve conter pelo menos um item. Cancele a venda caso queira desiste-la.");
        }

        ItemVenda item = itemVendaRepository.findById(itemId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Item nao encontrado com o ID: " + itemId));

        if (!item.getVenda().getId().equals(vendaId)) {
            throw new RegraNegocioException("O item informado nao pertence a esta venda");
        }

        // Repoe o estoque do item removido
        Variacao variacao = item.getVariacao();
        variacao.setEstoque(variacao.getEstoque() + item.getQuantidade());

        venda.removerItem(item);
        recalcularValorTotal(venda);
        return toResponse(vendaRepository.save(venda));
    }

    @Transactional
    public void excluir(Long id) {
        Venda venda = buscarEntidadePorId(id);

        if (venda.getStatus() == StatusVenda.ENTREGUE) {
            throw new ConflitoException("Vendas com status ENTREGUE nao podem ser excluidas do sistema");
        }

        // Se a venda nao estava cancelada, devolve os itens ao estoque antes de excluir
        if (venda.getStatus() != StatusVenda.CANCELADA) {
            for (ItemVenda item : venda.getItens()) {
                Variacao variacao = item.getVariacao();
                variacao.setEstoque(variacao.getEstoque() + item.getQuantidade());
            }
        }

        vendaRepository.delete(venda);
    }

    private void validarVendaPendente(Venda venda, String acao) {
        if (venda.getStatus() != StatusVenda.PENDENTE) {
            throw new ConflitoException(String.format(
                "Nao e permitido %s uma venda com status '%s'. Apenas vendas PENDENTE podem ser alteradas.",
                acao, venda.getStatus()
            ));
        }
    }

    private void recalcularValorTotal(Venda venda) {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemVenda item : venda.getItens()) {
            total = total.add(item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade())));
        }
        venda.setValorTotal(total);
    }

    public Venda buscarEntidadePorId(Long id) {
        return vendaRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Venda nao encontrada com o ID: " + id));
    }

    private VendaResponse toResponse(Venda venda) {
        List<ItemVendaResponse> itensDto = venda.getItens().stream()
            .map(item -> new ItemVendaResponse(
                item.getId(),
                item.getVariacao().getId(),
                item.getVariacao().getSku(),
                item.getVariacao().getTamanho(),
                item.getVariacao().getCor(),
                item.getVariacao().getProduto().getNome(),
                item.getQuantidade(),
                item.getPrecoUnitario(),
                item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade()))
            ))
            .toList();

        return new VendaResponse(
            venda.getId(),
            venda.getCliente().getId(),
            venda.getCliente().getNome(),
            venda.getDataVenda(),
            venda.getFormaPagamento(),
            venda.getStatus(),
            venda.getValorTotal(),
            itensDto
        );
    }
}
