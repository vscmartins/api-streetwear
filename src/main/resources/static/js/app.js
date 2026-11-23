
document.addEventListener('DOMContentLoaded', () => {

  const state = {
    produtos: [],
    marcas: [],
    clientes: [],
    clienteAtivo: null,
    pedidos: [],
    produtoParaExcluir: null,
    produtoParaComprar: null,
    variacoesDoProdutoComprar: [],
    filtroAtual: {
      busca: '',
      categoria: '',
      marca: ''
    }
  };


  const dom = {

    productsGrid: document.getElementById('products-grid'),
    loadingState: document.getElementById('loading-state'),
    errorState: document.getElementById('error-state'),
    errorStateMessage: document.getElementById('error-state-message'),
    emptyState: document.getElementById('empty-state'),
    navSearchInput: document.getElementById('nav-search-input'),
    filterCategoria: document.getElementById('filter-categoria'),
    filterMarca: document.getElementById('filter-marca'),
    btnResetFilters: document.getElementById('btn-reset-filters'),
    btnRetryFetch: document.getElementById('btn-retry-fetch'),
    btnEmptyClear: document.getElementById('btn-empty-clear'),


    selectActiveClient: document.getElementById('select-active-client'),
    ordersBadgeCount: document.getElementById('orders-badge-count'),
    btnCartCounter: document.getElementById('btn-cart-counter'),
    btnOpenCadastrar: document.getElementById('btn-open-cadastrar'),
    btnOpenPedidos: document.getElementById('btn-open-pedidos'),
    heroBtnCadastrar: document.getElementById('hero-btn-cadastrar'),
    footerBtnCadastrar: document.getElementById('footer-btn-cadastrar'),
    footerBtnPedidos: document.getElementById('footer-btn-pedidos'),


    modalCadastrar: document.getElementById('modal-cadastrar'),
    formCadastrar: document.getElementById('form-cadastrar-produto'),
    cadMarca: document.getElementById('cad-marca'),
    cadImagem: document.getElementById('cad-imagem'),
    imagePreview: document.getElementById('image-preview'),
    previewPlaceholder: document.getElementById('preview-placeholder'),
    btnCadLimpar: document.getElementById('btn-cad-limpar'),


    modalComprar: document.getElementById('modal-comprar'),
    formFinalizarCompra: document.getElementById('form-finalizar-compra'),
    buySneakerImg: document.getElementById('buy-sneaker-img'),
    buySneakerBrand: document.getElementById('buy-sneaker-brand'),
    buySneakerName: document.getElementById('buy-sneaker-name'),
    buySneakerPrice: document.getElementById('buy-sneaker-price'),
    buyVariacaoSelect: document.getElementById('buy-variacao-select'),
    buyStockInfo: document.getElementById('buy-stock-info'),
    buyQuantidade: document.getElementById('buy-quantidade'),
    buyPagamento: document.getElementById('buy-pagamento'),
    buyerSummaryName: document.getElementById('buyer-summary-name'),
    buyerSummaryEmail: document.getElementById('buyer-summary-email'),
    buyTotalCalc: document.getElementById('buy-total-calc'),
    btnConfirmPurchase: document.getElementById('btn-confirm-purchase'),


    modalPedidos: document.getElementById('modal-pedidos'),
    ordersTableBody: document.getElementById('orders-table-body'),
    pedidosLoading: document.getElementById('pedidos-loading'),
    pedidosEmpty: document.getElementById('pedidos-empty'),
    pedidosTabs: document.querySelectorAll('.tab-btn'),


    modalConfirmDelete: document.getElementById('modal-confirm-delete'),
    deletePromptText: document.getElementById('delete-prompt-text'),
    btnConfirmDeleteAction: document.getElementById('btn-confirm-delete-action'),


    toastContainer: document.getElementById('toast-container')
  };


  function showToast(title, message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;

    let icon = 'fa-info-circle';
    if (type === 'success') icon = 'fa-circle-check';
    if (type === 'error') icon = 'fa-triangle-exclamation';

    toast.innerHTML = `
      <i class="fa-solid ${icon}" style="font-size: 1.2rem; color: var(--${type === 'info' ? 'primary' : type}); margin-top: 2px;"></i>
      <div class="toast-content">
        <h4 class="toast-title">${title}</h4>
        <p class="toast-message">${message}</p>
      </div>
      <button class="toast-close" aria-label="Fechar">&times;</button>
    `;

    const closeBtn = toast.querySelector('.toast-close');
    closeBtn.addEventListener('click', () => toast.remove());

    dom.toastContainer.appendChild(toast);

    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateX(100%)';
      toast.style.transition = 'all 0.3s ease';
      setTimeout(() => toast.remove(), 300);
    }, 4500);
  }


  const formatMoney = (val) => {
    return Number(val).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  };

  const formatDate = (isoStr) => {
    if (!isoStr) return '';
    const d = new Date(isoStr);
    return d.toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
  };


  async function carregarClientes() {
    try {
      const res = await fetch('/api/clientes');
      if (!res.ok) throw new Error('Não foi possível carregar os clientes');

      const clientes = await res.json();
      state.clientes = clientes;

      dom.selectActiveClient.innerHTML = '';
      if (clientes.length === 0) {
        dom.selectActiveClient.innerHTML = '<option value="">Nenhum cliente</option>';
        return;
      }

      clientes.forEach((cli, idx) => {
        const opt = document.createElement('option');
        opt.value = cli.id;
        opt.textContent = cli.nome.trim().split(/\s+/)[0];
        dom.selectActiveClient.appendChild(opt);
      });


      state.clienteAtivo = clientes[0];
      dom.selectActiveClient.value = state.clienteAtivo.id;
      atualizarResumoClienteAtivo();
    } catch (err) {
      console.warn('Erro ao carregar clientes:', err);
    }
  }

  function atualizarResumoClienteAtivo() {
    if (!state.clienteAtivo) return;
    dom.buyerSummaryName.textContent = state.clienteAtivo.nome;
    dom.buyerSummaryEmail.textContent = state.clienteAtivo.email;
  }

  dom.selectActiveClient.addEventListener('change', (e) => {
    const cliId = Number(e.target.value);
    state.clienteAtivo = state.clientes.find(c => c.id === cliId) || null;
    atualizarResumoClienteAtivo();
    showToast('Cliente Ativo Alterado', `Agora você está navegando como ${state.clienteAtivo.nome}`, 'info');
    carregarPedidos();
  });


  async function carregarMarcas() {
    try {
      const res = await fetch('/api/marcas');
      if (!res.ok) throw new Error('Falha ao carregar marcas');

      const marcas = await res.json();
      state.marcas = marcas;


      dom.filterMarca.innerHTML = '<option value="">Todas as Marcas</option>';
      marcas.forEach(m => {
        const opt = document.createElement('option');
        opt.value = m.id;
        opt.textContent = m.nome;
        dom.filterMarca.appendChild(opt);
      });


      dom.cadMarca.innerHTML = '<option value="">Selecione uma Marca</option>';
      marcas.forEach(m => {
        const opt = document.createElement('option');
        opt.value = m.id;
        opt.textContent = m.nome;
        dom.cadMarca.appendChild(opt);
      });
    } catch (err) {
      console.warn('Erro ao carregar marcas:', err);
    }
  }


  async function carregarCatalogo() {
    dom.loadingState.classList.remove('hidden');
    dom.errorState.classList.add('hidden');
    dom.emptyState.classList.add('hidden');
    dom.productsGrid.innerHTML = '';

    const params = new URLSearchParams();
    if (state.filtroAtual.busca) params.append('busca', state.filtroAtual.busca);
    if (state.filtroAtual.categoria) params.append('categoria', state.filtroAtual.categoria);
    if (state.filtroAtual.marca) params.append('marca', state.filtroAtual.marca);

    const url = `/api/produtos?${params.toString()}`;

    try {
      const res = await fetch(url);
      if (!res.ok) {
        const errJson = await res.json().catch(() => ({}));
        throw new Error(errJson.mensagem || `Erro ${res.status}: Não foi possível obter o catálogo`);
      }

      const produtos = await res.json();
      state.produtos = produtos;
      dom.loadingState.classList.add('hidden');

      if (produtos.length === 0) {
        dom.emptyState.classList.remove('hidden');
      } else {
        renderizarCards(produtos);
      }
    } catch (err) {
      dom.loadingState.classList.add('hidden');
      dom.errorState.classList.remove('hidden');
      dom.errorStateMessage.textContent = err.message || 'Falha de comunicação com o backend.';
    }
  }


  function renderizarCards(produtos) {
    dom.productsGrid.innerHTML = '';

    produtos.forEach(prod => {
      const card = document.createElement('article');
      card.className = 'product-card';
      card.dataset.id = prod.id;

      const fallbackImg = 'https://images.unsplash.com/photo-1552346154-21d32810aba3?w=600&auto=format&fit=crop&q=80';
      const imgSrc = prod.imagemUrl ? prod.imagemUrl : fallbackImg;
      const semEstoque = Number(prod.estoqueTotal || 0) <= 0;

      card.innerHTML = `
        <div class="card-img-wrapper">
          <span class="card-badge${semEstoque ? ' card-badge-sold-out' : ''}">${semEstoque ? 'ESGOTADO' : 'DROP OFICIAL'}</span>
          <button class="card-favorite-btn" title="Adicionar aos Favoritos">
            <i class="fa-regular fa-heart"></i>
          </button>
          <img src="${imgSrc}" alt="${prod.nome}" class="card-img" onerror="this.src='${fallbackImg}'">
        </div>
        <div class="card-body">
          <div class="card-meta">
            <span class="card-category">${prod.categoria}</span>
            <span class="card-rating"><i class="fa-solid fa-star"></i> 4.9</span>
          </div>
          <h3 class="card-title">${prod.nome}</h3>
          <span class="card-brand">${prod.marcaNome || 'Streetwear'}</span>
          <div class="card-footer">
            <div class="card-price">${formatMoney(prod.preco)}</div>
            <div class="card-actions">
              <button class="btn-card-del" title="Excluir Tênis" data-action="delete" data-id="${prod.id}">
                <i class="fa-solid fa-trash-can"></i>
              </button>
              <button class="btn-card-buy" data-action="buy" data-id="${prod.id}">
                <i class="fa-solid ${semEstoque ? 'fa-ban' : 'fa-cart-shopping'}"></i> ${semEstoque ? 'ESGOTADO' : 'COMPRAR'}
              </button>
            </div>
          </div>
        </div>
      `;


      const favBtn = card.querySelector('.card-favorite-btn');
      favBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        favBtn.classList.toggle('favorited');
        const icon = favBtn.querySelector('i');
        if (favBtn.classList.contains('favorited')) {
          icon.className = 'fa-solid fa-heart';
          showToast('Favoritos', `"${prod.nome}" salvo nos favoritos.`, 'info');
        } else {
          icon.className = 'fa-regular fa-heart';
        }
      });

      const delBtn = card.querySelector('[data-action="delete"]');
      delBtn.addEventListener('click', () => abrirModalExcluir(prod));


      const buyBtn = card.querySelector('[data-action="buy"]');
      buyBtn.disabled = semEstoque;
      if (!semEstoque) {
        buyBtn.addEventListener('click', () => abrirModalComprar(prod));
      }

      dom.productsGrid.appendChild(card);
    });
  }

  let debounceTimer;
  dom.navSearchInput.addEventListener('input', (e) => {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => {
      state.filtroAtual.busca = e.target.value.trim();
      carregarCatalogo();
    }, 350);
  });

  dom.filterCategoria.addEventListener('change', (e) => {
    state.filtroAtual.categoria = e.target.value;
    carregarCatalogo();
  });

  dom.filterMarca.addEventListener('change', (e) => {
    state.filtroAtual.marca = e.target.value;
    carregarCatalogo();
  });

  dom.btnResetFilters.addEventListener('click', () => {
    state.filtroAtual.busca = '';
    state.filtroAtual.categoria = '';
    state.filtroAtual.marca = '';
    dom.navSearchInput.value = '';
    dom.filterCategoria.value = '';
    dom.filterMarca.value = '';
    carregarCatalogo();
  });

  dom.btnRetryFetch.addEventListener('click', carregarCatalogo);
  dom.btnEmptyClear.addEventListener('click', () => dom.btnResetFilters.click());


  document.querySelectorAll('.category-card').forEach(catCard => {
    catCard.addEventListener('click', () => {
      const cat = catCard.dataset.cat;
      dom.filterCategoria.value = cat;
      state.filtroAtual.categoria = cat;
      carregarCatalogo();
      document.getElementById('produtos-section').scrollIntoView({ behavior: 'smooth' });
    });
  });

  dom.cadImagem.addEventListener('input', (e) => {
    const url = e.target.value.trim();
    if (url.startsWith('http')) {
      dom.imagePreview.src = url;
      dom.imagePreview.classList.remove('hidden');
      dom.previewPlaceholder.classList.add('hidden');
      dom.imagePreview.onerror = () => {
        dom.imagePreview.classList.add('hidden');
        dom.previewPlaceholder.classList.remove('hidden');
      };
    } else {
      dom.imagePreview.classList.add('hidden');
      dom.previewPlaceholder.classList.remove('hidden');
    }
  });

  dom.btnCadLimpar.addEventListener('click', () => {
    dom.formCadastrar.reset();
    dom.imagePreview.classList.add('hidden');
    dom.previewPlaceholder.classList.remove('hidden');
  });

  function openModal(modal) {
    modal.classList.remove('hidden');
    document.body.style.overflow = 'hidden';
  }

  function closeModal(modal) {
    modal.classList.add('hidden');
    document.body.style.overflow = '';
  }

  document.querySelectorAll('[data-close-modal]').forEach(btn => {
    btn.addEventListener('click', () => {
      const modalId = btn.dataset.closeModal;
      const modal = document.getElementById(modalId);
      if (modal) closeModal(modal);
    });
  });

  window.addEventListener('click', (e) => {
    if (e.target.classList.contains('modal-overlay')) {
      closeModal(e.target);
    }
  });


  const openCadastrarHandlers = [dom.btnOpenCadastrar, dom.heroBtnCadastrar, dom.footerBtnCadastrar];
  openCadastrarHandlers.forEach(btn => {
    if (btn) btn.addEventListener('click', () => openModal(dom.modalCadastrar));
  });


  const openPedidosHandlers = [dom.btnOpenPedidos, dom.btnCartCounter, dom.footerBtnPedidos];
  openPedidosHandlers.forEach(btn => {
    if (btn) btn.addEventListener('click', () => {
      openModal(dom.modalPedidos);
      carregarPedidos();
    });
  });


  dom.formCadastrar.addEventListener('submit', async (e) => {
    e.preventDefault();

    const nome = document.getElementById('cad-nome').value.trim();
    const descricao = document.getElementById('cad-descricao').value.trim();
    const marcaId = dom.cadMarca.value;
    const categoria = document.getElementById('cad-categoria').value;
    const tamanho = document.getElementById('cad-tamanho').value;
    const quantidade = parseInt(document.getElementById('cad-quantidade').value, 10);
    const preco = parseFloat(document.getElementById('cad-valor').value);
    const imagemUrl = dom.cadImagem.value.trim();

    if (!nome || !descricao || !marcaId || !categoria || isNaN(preco) || isNaN(quantidade)) {
      showToast('Campos Obrigatórios', 'Por favor, preencha todos os campos sinalizados com *.', 'error');
      return;
    }

    const saveBtn = document.getElementById('btn-cad-salvar');
    const originalText = saveBtn.innerHTML;
    saveBtn.disabled = true;
    saveBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Cadastrando...';

    try {

      const prodPayload = {
        nome,
        descricao,
        preco,
        categoria,
        ativo: true,
        imagemUrl: imagemUrl || null,
        marcaId: Number(marcaId)
      };

      const resProd = await fetch('/api/produtos', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(prodPayload)
      });

      if (!resProd.ok) {
        const errJson = await resProd.json().catch(() => ({}));
        throw new Error(errJson.mensagem || 'Falha ao cadastrar produto');
      }

      const prodCriado = await resProd.json();


      const skuLimpo = nome.substring(0, 3).toUpperCase() + '-' + Date.now().toString().slice(-4) + '-' + tamanho.replace(/\s+/g, '');
      const varPayload = {
        sku: skuLimpo,
        tamanho: tamanho,
        cor: 'Original',
        estoque: quantidade,
        estoqueMinimo: 2,
        produtoId: prodCriado.id
      };

      await fetch('/api/variacoes', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(varPayload)
      }).catch(err => console.warn('Aviso: Variação inicial:', err));


      showToast('Silhueta Adicionada!', `Tênis "${prodCriado.nome}" cadastrado com sucesso no mostruário.`, 'success');
      closeModal(dom.modalCadastrar);
      dom.formCadastrar.reset();
      dom.imagePreview.classList.add('hidden');
      dom.previewPlaceholder.classList.remove('hidden');


      carregarCatalogo();
    } catch (err) {
      showToast('Erro ao Salvar', err.message, 'error');
    } finally {
      saveBtn.disabled = false;
      saveBtn.innerHTML = originalText;
    }
  });


  function abrirModalExcluir(produto) {
    state.produtoParaExcluir = produto;
    dom.deletePromptText.textContent = `Tem certeza de que deseja remover "${produto.nome}" do catálogo? As variações e os itens desse produto nos pedidos também serão apagados, e os totais serão recalculados.`;
    openModal(dom.modalConfirmDelete);
  }

  dom.btnConfirmDeleteAction.addEventListener('click', async () => {
    if (!state.produtoParaExcluir) return;
    const prodId = state.produtoParaExcluir.id;
    const nome = state.produtoParaExcluir.nome;

    dom.btnConfirmDeleteAction.disabled = true;
    dom.btnConfirmDeleteAction.textContent = 'Excluindo...';

    try {
      const res = await fetch(`/api/produtos/${prodId}`, {
        method: 'DELETE'
      });

      if (!res.ok) {
        const errJson = await res.json().catch(() => ({}));
        throw new Error(errJson.mensagem || 'Não foi possível excluir o produto. Verifique se ele possui vendas vinculadas.');
      }

      showToast('Produto Removido', `O produto "${nome}" e seus itens nos pedidos foram excluídos.`, 'success');
      closeModal(dom.modalConfirmDelete);
      state.produtoParaExcluir = null;


      const cardEl = document.querySelector(`.product-card[data-id="${prodId}"]`);
      if (cardEl) {
        cardEl.style.transition = 'all 0.3s ease';
        cardEl.style.transform = 'scale(0.8)';
        cardEl.style.opacity = '0';
        setTimeout(() => cardEl.remove(), 300);
      }
    } catch (err) {
      showToast('Erro na Exclusão', err.message, 'error');
    } finally {
      dom.btnConfirmDeleteAction.disabled = false;
      dom.btnConfirmDeleteAction.textContent = 'Sim, Excluir';
    }
  });


  async function abrirModalComprar(produto) {
    state.produtoParaComprar = produto;
    dom.buySneakerName.textContent = produto.nome;
    dom.buySneakerBrand.textContent = produto.marcaNome || 'Streetwear';
    dom.buySneakerPrice.textContent = formatMoney(produto.preco);
    dom.buySneakerImg.src = produto.imagemUrl || 'https://images.unsplash.com/photo-1552346154-21d32810aba3?w=600&auto=format&fit=crop&q=80';
    dom.buyQuantidade.value = 1;
    dom.buyQuantidade.max = 1;
    dom.buyTotalCalc.textContent = formatMoney(produto.preco);
    dom.btnConfirmPurchase.disabled = true;

    dom.buyVariacaoSelect.innerHTML = '<option value="">Carregando tamanhos disponíveis...</option>';
    openModal(dom.modalComprar);

    try {
      const res = await fetch(`/api/variacoes/produto/${produto.id}`);
      if (!res.ok) throw new Error('Não foi possível consultar o estoque deste produto.');
      const variacoes = await res.json();

      state.variacoesDoProdutoComprar = variacoes;
      dom.buyVariacaoSelect.innerHTML = '';

      if (variacoes.length === 0) {
        const opt = document.createElement('option');
        opt.value = '';
        opt.textContent = 'Sem estoque cadastrado';
        opt.disabled = true;
        dom.buyVariacaoSelect.appendChild(opt);
        dom.buyStockInfo.textContent = 'Este produto não possui estoque cadastrado.';
        return;
      } else {
        variacoes.forEach(v => {
          const opt = document.createElement('option');
          opt.value = v.id;
          opt.textContent = `Tam ${v.tamanho} - ${v.cor} (${v.estoque} disponíveis)`;
          if (v.estoque <= 0) {
            opt.disabled = true;
            opt.textContent += ' [ESGOTADO]';
          }
          dom.buyVariacaoSelect.appendChild(opt);
        });


        const disponivel = variacoes.find(v => v.estoque > 0);
        if (disponivel) {
          dom.buyVariacaoSelect.value = disponivel.id;
          dom.buyStockInfo.textContent = `Estoque disponível para este SKU: ${disponivel.estoque} unidades`;
          dom.buyQuantidade.max = disponivel.estoque;
          dom.btnConfirmPurchase.disabled = false;
        } else {
          dom.buyStockInfo.textContent = 'Atenção: Este produto está sem estoque no momento.';
        }
      }
    } catch (err) {
      console.warn('Erro ao obter variações:', err);
      dom.buyVariacaoSelect.innerHTML = '<option value="">Estoque indisponível</option>';
      dom.buyStockInfo.textContent = err.message;
      showToast('Erro ao consultar estoque', err.message, 'error');
    }
  }

  dom.buyQuantidade.addEventListener('input', () => {
    if (!state.produtoParaComprar) return;
    const max = Number(dom.buyQuantidade.max) || 1;
    const qtd = Math.min(max, Math.max(1, parseInt(dom.buyQuantidade.value, 10) || 1));
    dom.buyQuantidade.value = qtd;
    const total = state.produtoParaComprar.preco * qtd;
    dom.buyTotalCalc.textContent = formatMoney(total);
  });

  dom.buyVariacaoSelect.addEventListener('change', (e) => {
    const varId = Number(e.target.value);
    const variacao = state.variacoesDoProdutoComprar.find(v => v.id === varId);
    if (variacao) {
      dom.buyStockInfo.textContent = `Estoque disponível para este SKU: ${variacao.estoque} unidades`;
      dom.buyQuantidade.max = variacao.estoque;
      dom.buyQuantidade.value = Math.min(Number(dom.buyQuantidade.value) || 1, variacao.estoque);
      dom.buyTotalCalc.textContent = formatMoney(state.produtoParaComprar.preco * dom.buyQuantidade.value);
      dom.btnConfirmPurchase.disabled = variacao.estoque <= 0;
    }
  });

  dom.formFinalizarCompra.addEventListener('submit', async (e) => {
    e.preventDefault();

    if (!state.clienteAtivo) {
      showToast('Cliente Não Definido', 'Selecione ou crie um cliente ativo antes de comprar.', 'error');
      return;
    }

    if (!state.produtoParaComprar) return;

    const variacaoId = Number(dom.buyVariacaoSelect.value);
    const quantidade = parseInt(dom.buyQuantidade.value, 10);
    const formaPagamento = dom.buyPagamento.value;
    const variacao = state.variacoesDoProdutoComprar.find(item => item.id === variacaoId);
    if (!variacao || !Number.isInteger(quantidade) || quantidade < 1 || quantidade > variacao.estoque) {
      showToast('Estoque indisponível', 'Selecione uma variação com estoque suficiente.', 'error');
      return;
    }

    const payload = {
      clienteId: state.clienteAtivo.id,
      formaPagamento: formaPagamento,
      itens: [
        {
          variacaoId: Number(variacaoId),
          quantidade: quantidade
        }
      ]
    };

    dom.btnConfirmPurchase.disabled = true;
    dom.btnConfirmPurchase.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Processando...';

    try {
      const res = await fetch('/api/vendas', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (!res.ok) {
        const errJson = await res.json().catch(() => ({}));
        throw new Error(errJson.mensagem || 'Falha ao realizar a compra.');
      }

      const venda = await res.json();

      showToast('Compra Finalizada!', `Pedido #${venda.id} criado com sucesso no nome de ${venda.clienteNome}. Estoque debitado!`, 'success');
      closeModal(dom.modalComprar);

      await Promise.all([carregarPedidos(), carregarCatalogo()]);
    } catch (err) {
      showToast('Erro na Compra', err.message, 'error');
    } finally {
      dom.btnConfirmPurchase.disabled = false;
      dom.btnConfirmPurchase.innerHTML = '<i class="fa-solid fa-check"></i> FINALIZAR COMPRA';
    }
  });


  let currentTab = 'todos';

  dom.pedidosTabs.forEach(tab => {
    tab.addEventListener('click', () => {
      dom.pedidosTabs.forEach(t => t.classList.remove('active'));
      tab.classList.add('active');
      currentTab = tab.dataset.tab;
      renderizarTabelaPedidos();
    });
  });

  async function carregarPedidos() {
    dom.pedidosLoading.classList.remove('hidden');
    dom.pedidosEmpty.classList.add('hidden');
    dom.ordersTableBody.innerHTML = '';

    try {
      const res = await fetch('/api/vendas');
      if (!res.ok) throw new Error('Não foi possível carregar os pedidos');

      const pedidos = await res.json();
      state.pedidos = pedidos;


      dom.ordersBadgeCount.textContent = pedidos.length;

      dom.pedidosLoading.classList.add('hidden');
      renderizarTabelaPedidos();
    } catch (err) {
      dom.pedidosLoading.classList.add('hidden');
      console.warn('Erro ao carregar vendas:', err);
    }
  }

  function renderizarTabelaPedidos() {
    dom.ordersTableBody.innerHTML = '';

    let lista = state.pedidos;
    if (currentTab === 'cliente' && state.clienteAtivo) {
      lista = lista.filter(v => v.clienteId === state.clienteAtivo.id);
    }

    if (lista.length === 0) {
      dom.pedidosEmpty.classList.remove('hidden');
      return;
    }
    dom.pedidosEmpty.classList.add('hidden');

    lista.forEach(venda => {
      const tr = document.createElement('tr');

      const itensDesc = venda.itens.map(i => `${i.quantidade}x ${i.produtoNome} (Tam ${i.tamanho})`).join('<br>');
      const statusClass = `status-${venda.status.toLowerCase()}`;


      let acoesHtml = '';
      if (venda.status === 'PENDENTE') {
        acoesHtml = `
          <div class="table-action-btns">
            <button class="btn-status-step" data-action="status" data-id="${venda.id}" data-next="PAGA">
              <i class="fa-solid fa-dollar-sign"></i> Pagar
            </button>
            <button class="btn-status-cancel" data-action="status" data-id="${venda.id}" data-next="CANCELADA">
              <i class="fa-solid fa-ban"></i> Cancelar
            </button>
          </div>
        `;
      } else if (venda.status === 'PAGA') {
        acoesHtml = `
          <div class="table-action-btns">
            <button class="btn-status-step" data-action="status" data-id="${venda.id}" data-next="ENVIADA">
              <i class="fa-solid fa-truck"></i> Despachar
            </button>
            <button class="btn-status-cancel" data-action="status" data-id="${venda.id}" data-next="CANCELADA">
              <i class="fa-solid fa-ban"></i> Cancelar
            </button>
          </div>
        `;
      } else if (venda.status === 'ENVIADA') {
        acoesHtml = `
          <button class="btn-status-step" data-action="status" data-id="${venda.id}" data-next="ENTREGUE">
            <i class="fa-solid fa-box-check"></i> Confirmar Entrega
          </button>
        `;
      } else if (venda.status === 'ENTREGUE') {
        acoesHtml = '<span style="color: var(--success); font-weight: 700; font-size: 0.8rem;"><i class="fa-solid fa-circle-check"></i> Concluído</span>';
      } else if (venda.status === 'CANCELADA') {
        acoesHtml = '<span style="color: var(--danger); font-weight: 700; font-size: 0.8rem;"><i class="fa-solid fa-circle-xmark"></i> Estoque Devolvido</span>';
      }

      tr.innerHTML = `
        <td><strong>#${venda.id}</strong></td>
        <td>${formatDate(venda.dataVenda)}</td>
        <td>${venda.clienteNome}</td>
        <td>${itensDesc}</td>
        <td>${venda.formaPagamento}</td>
        <td><strong>${formatMoney(venda.valorTotal)}</strong></td>
        <td><span class="status-badge ${statusClass}">${venda.status}</span></td>
        <td>${acoesHtml}</td>
      `;


      tr.querySelectorAll('[data-action="status"]').forEach(btn => {
        btn.addEventListener('click', () => {
          const nextStatus = btn.dataset.next;
          atualizarStatusPedido(venda.id, nextStatus);
        });
      });

      dom.ordersTableBody.appendChild(tr);
    });
  }

  async function atualizarStatusPedido(vendaId, novoStatus) {
    try {
      const res = await fetch(`/api/vendas/${vendaId}/status`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status: novoStatus })
      });

      if (!res.ok) {
        const errJson = await res.json().catch(() => ({}));
        throw new Error(errJson.mensagem || 'Não foi possível atualizar o status');
      }

      showToast('Status Atualizado', `Pedido #${vendaId} agora está com status ${novoStatus}!`, 'info');
      await carregarPedidos();

      if (novoStatus === 'CANCELADA') {
        carregarCatalogo();
      }
    } catch (err) {
      showToast('Falha na Transição', err.message, 'error');
    }
  }


  async function init() {
    await carregarClientes();
    await carregarMarcas();
    await carregarCatalogo();
    await carregarPedidos();
  }

  init();
});
