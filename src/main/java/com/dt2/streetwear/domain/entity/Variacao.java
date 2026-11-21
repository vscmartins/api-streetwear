package com.dt2.streetwear.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "variacao",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_variacao_sku", columnNames = {"sku"}),
        @UniqueConstraint(name = "uk_variacao_produto_tamanho_cor", columnNames = {"produto_id", "tamanho", "cor"})
    }
)
public class Variacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String sku;

    @Column(nullable = false, length = 10)
    private String tamanho;

    @Column(nullable = false, length = 40)
    private String cor;

    @Column(nullable = false)
    private Integer estoque = 0;

    @Column(name = "estoque_minimo", nullable = false)
    private Integer estoqueMinimo = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    protected Variacao() {
    }

    public Variacao(String sku, String tamanho, String cor, Integer estoque, Integer estoqueMinimo, Produto produto) {
        this.sku = sku;
        this.tamanho = tamanho;
        this.cor = cor;
        this.estoque = estoque != null ? estoque : 0;
        this.estoqueMinimo = estoqueMinimo != null ? estoqueMinimo : 0;
        this.produto = produto;
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getTamanho() {
        return tamanho;
    }

    public void setTamanho(String tamanho) {
        this.tamanho = tamanho;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public Integer getEstoque() {
        return estoque;
    }

    public void setEstoque(Integer estoque) {
        this.estoque = estoque;
    }

    public Integer getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public void setEstoqueMinimo(Integer estoqueMinimo) {
        this.estoqueMinimo = estoqueMinimo;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }
}
