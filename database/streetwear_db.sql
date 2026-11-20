CREATE DATABASE IF NOT EXISTS streetwear_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE streetwear_db;

CREATE TABLE IF NOT EXISTS marca (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     nome VARCHAR(60) NOT NULL,
    pais_origem VARCHAR(60) NULL,
    descricao VARCHAR(255) NULL,
    CONSTRAINT uk_marca_nome UNIQUE (nome)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS produto (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       nome VARCHAR(120) NOT NULL,
    descricao VARCHAR(500) NOT NULL,
    preco DECIMAL(10, 2) NOT NULL,
    categoria ENUM('TENIS', 'MOLETOM', 'JAQUETA', 'BONE', 'CAMISETA') NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    imagem_url VARCHAR(500) NULL,
    marca_id BIGINT NOT NULL,
    CONSTRAINT fk_produto_marca FOREIGN KEY (marca_id) REFERENCES marca (id) ON DELETE RESTRICT,
    CONSTRAINT chk_produto_preco CHECK (preco > 0)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_produto_marca ON produto (marca_id);
CREATE INDEX idx_produto_categoria ON produto (categoria);
CREATE INDEX idx_produto_ativo ON produto (ativo);

CREATE TABLE IF NOT EXISTS variacao (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        sku VARCHAR(40) NOT NULL,
    tamanho VARCHAR(10) NOT NULL,
    cor VARCHAR(40) NOT NULL,
    estoque INT NOT NULL DEFAULT 0,
    estoque_minimo INT NOT NULL DEFAULT 0,
    produto_id BIGINT NOT NULL,
    CONSTRAINT uk_variacao_sku UNIQUE (sku),
    CONSTRAINT uk_variacao_produto_tamanho_cor UNIQUE (produto_id, tamanho, cor),
    CONSTRAINT fk_variacao_produto FOREIGN KEY (produto_id) REFERENCES produto (id) ON DELETE RESTRICT,
    CONSTRAINT chk_variacao_estoque CHECK (estoque >= 0),
    CONSTRAINT chk_variacao_estoque_min CHECK (estoque_minimo >= 0)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_variacao_produto ON variacao (produto_id);
CREATE INDEX idx_variacao_estoque ON variacao (estoque);

CREATE TABLE IF NOT EXISTS cliente (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       nome VARCHAR(120) NOT NULL,
    cpf VARCHAR(11) NOT NULL,
    email VARCHAR(120) NOT NULL,
    telefone VARCHAR(20) NULL,
    instagram VARCHAR(40) NULL,
    CONSTRAINT uk_cliente_cpf UNIQUE (cpf),
    CONSTRAINT uk_cliente_email UNIQUE (email)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS venda (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     cliente_id BIGINT NOT NULL,
                                     data_venda DATETIME NOT NULL,
                                     forma_pagamento ENUM('PIX', 'CARTAO_CREDITO', 'CARTAO_DEBITO', 'DINHEIRO') NOT NULL,
    status ENUM('PENDENTE', 'PAGA', 'ENVIADA', 'ENTREGUE', 'CANCELADA') NOT NULL,
    valor_total DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_venda_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id) ON DELETE RESTRICT,
    CONSTRAINT chk_venda_total CHECK (valor_total >= 0)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_venda_cliente ON venda (cliente_id);
CREATE INDEX idx_venda_status ON venda (status);
CREATE INDEX idx_venda_data ON venda (data_venda);

CREATE TABLE IF NOT EXISTS item_venda (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          venda_id BIGINT NOT NULL,
                                          variacao_id BIGINT NOT NULL,
                                          quantidade INT NOT NULL,
                                          preco_unitario DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_item_venda_venda FOREIGN KEY (venda_id) REFERENCES venda (id) ON DELETE CASCADE,
    CONSTRAINT fk_item_venda_variacao FOREIGN KEY (variacao_id) REFERENCES variacao (id) ON DELETE RESTRICT,
    CONSTRAINT chk_item_venda_qtd CHECK (quantidade >= 1),
    CONSTRAINT chk_item_venda_preco CHECK (preco_unitario > 0)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_item_venda_venda ON item_venda (venda_id);
CREATE INDEX idx_item_venda_variacao ON item_venda (variacao_id);

INSERT INTO marca (id, nome, pais_origem, descricao)
SELECT * FROM (
                  SELECT 1 AS id, 'Nike' AS nome, 'Estados Unidos' AS pais_origem, 'Lider mundial em artigos esportivos e cultura sneakerhead' AS descricao UNION ALL
                  SELECT 2, 'Adidas', 'Alemanha', 'Classicos iconicos das tres listras no hip-hop e skate' UNION ALL
                  SELECT 3, 'Jordan', 'Estados Unidos', 'A marca do Jumpman, sinonimo supremo de basquete e streetwear' UNION ALL
                  SELECT 4, 'Puma', 'Alemanha', 'Pioneira na conexao entre b-boys do Bronx e calçados urbanos' UNION ALL
                  SELECT 5, 'New Balance', 'Estados Unidos', 'Silhuetas retro com conforto premium e hype urbano' UNION ALL
                  SELECT 6, 'Carhartt', 'Estados Unidos', 'Workwear classico adotado pela cultura rap e skate nova-iorquina' UNION ALL
                  SELECT 7, 'Champion', 'Estados Unidos', 'Criadora do hoodie reverso autentico e estilo universitario' UNION ALL
                  SELECT 8, 'New Era', 'Estados Unidos', 'O boné oficial dos caps 59FIFTY indispensavel no streetwear'
              ) AS seed
WHERE NOT EXISTS (SELECT 1 FROM marca WHERE marca.id = seed.id);

INSERT INTO produto (id, nome, descricao, preco, categoria, ativo, marca_id)
SELECT * FROM (
                  SELECT 1 AS id, 'Air Force 1 ''07 Triple White' AS nome, 'O tenis branco classico que define o estilo do rap novaiorquino desde os anos 80.' AS descricao, 799.90 AS preco, 'TENIS' AS categoria, TRUE AS ativo, 1 AS marca_id UNION ALL
                  SELECT 2, 'Dunk Low Retro Panda', 'Silhueta iconica bicolor preto e branco, essencial em qualquer colecao urbana.', 899.90, 'TENIS', TRUE, 1 UNION ALL
                  SELECT 3, 'Air Jordan 1 High OG Chicago', 'O tenis mais historico do hip-hop, trazendo as cores dos Bulls e silhueta lendaria.', 1499.90, 'TENIS', TRUE, 3 UNION ALL
                  SELECT 4, 'Adidas Superstar Core Black', 'O classico shell-toe imortalizado pelo Run-DMC no Queens, Nova York.', 549.90, 'TENIS', TRUE, 2 UNION ALL
                  SELECT 5, 'Adidas Samba OG Cloud White', 'Classico do futebol de rua que conquistou o streetwear contemporaneo.', 699.90, 'TENIS', TRUE, 2 UNION ALL
                  SELECT 6, 'Tech Fleece Full-Zip Hoodie Black', 'Moletom tecnologico com corte atletico moderno e isolamento termico leve.', 649.90, 'MOLETOM', TRUE, 1 UNION ALL
                  SELECT 7, 'Reverse Weave Hoodie Grey', 'Moletom pesado de alta densidade resistente ao encolhimento, pura tradicao rap.', 499.90, 'MOLETOM', TRUE, 7 UNION ALL
                  SELECT 8, 'Carhartt WIP Detroit Jacket Hamilton Brown', 'Jaqueta de lona de algodao resistente com forro aquecido e gola de veludo.', 1299.90, 'JAQUETA', TRUE, 6 UNION ALL
                  SELECT 9, 'New Era 59FIFTY New York Yankees Navy', 'O tradicional bone fitted de aba reta dos Yankees com o logo bordado frontal.', 249.90, 'BONE', TRUE, 8 UNION ALL
                  SELECT 10, 'Puma Downtown Graphic Oversized Tee', 'Camiseta com caimento solto e estampa urbana inspirada nas ruas do Brooklyn.', 199.90, 'CAMISETA', TRUE, 4
              ) AS seed
WHERE NOT EXISTS (SELECT 1 FROM produto WHERE produto.id = seed.id);

INSERT INTO variacao (id, sku, tamanho, cor, estoque, estoque_minimo, produto_id)
SELECT * FROM (
                  SELECT 1 AS id, 'NIK-AF1-WHT-40' AS sku, '40' AS tamanho, 'Branco' AS cor, 8 AS estoque, 2 AS estoque_minimo, 1 AS produto_id UNION ALL
                  SELECT 2, 'NIK-AF1-WHT-41', '41', 'Branco', 5, 2, 1 UNION ALL
                  SELECT 3, 'NIK-AF1-WHT-42', '42', 'Branco', 1, 3, 1 UNION ALL
                  SELECT 4, 'NIK-AF1-WHT-43', '43', 'Branco', 0, 2, 1 UNION ALL
                  SELECT 5, 'NIK-DNK-PND-40', '40', 'Preto/Branco', 4, 2, 2 UNION ALL
                  SELECT 6, 'NIK-DNK-PND-41', '41', 'Preto/Branco', 2, 2, 2 UNION ALL
                  SELECT 7, 'NIK-DNK-PND-42', '42', 'Preto/Branco', 0, 2, 2 UNION ALL
                  SELECT 8, 'JOR-AJ1-CHI-41', '41', 'Vermelho/Branco', 3, 1, 3 UNION ALL
                  SELECT 9, 'JOR-AJ1-CHI-42', '42', 'Vermelho/Branco', 1, 1, 3 UNION ALL
                  SELECT 10, 'ADI-SUP-BLK-40', '40', 'Preto', 6, 2, 4 UNION ALL
                  SELECT 11, 'ADI-SUP-BLK-42', '42', 'Preto', 4, 2, 4 UNION ALL
                  SELECT 12, 'ADI-SAM-WHT-41', '41', 'Branco/Preto', 5, 2, 5 UNION ALL
                  SELECT 13, 'ADI-SAM-WHT-42', '42', 'Branco/Preto', 2, 3, 5 UNION ALL
                  SELECT 14, 'NIK-TCF-BLK-M', 'M', 'Preto', 7, 2, 6 UNION ALL
                  SELECT 15, 'NIK-TCF-BLK-G', 'G', 'Preto', 3, 2, 6 UNION ALL
                  SELECT 16, 'NIK-TCF-BLK-GG', 'GG', 'Preto', 1, 2, 6 UNION ALL
                  SELECT 17, 'CHP-REV-GRY-M', 'M', 'Cinza', 5, 2, 7 UNION ALL
                  SELECT 18, 'CHP-REV-GRY-G', 'G', 'Cinza', 4, 2, 7 UNION ALL
                  SELECT 19, 'CAR-DET-BRN-M', 'M', 'Marrom', 2, 1, 8 UNION ALL
                  SELECT 20, 'CAR-DET-BRN-G', 'G', 'Marrom', 1, 1, 8 UNION ALL
                  SELECT 21, 'NWE-59F-NYN-714', '7 1/4', 'Azul Marinho', 10, 3, 9 UNION ALL
                  SELECT 22, 'NWE-59F-NYN-738', '7 3/8', 'Azul Marinho', 8, 3, 9 UNION ALL
                  SELECT 23, 'PUM-TEE-BLK-M', 'M', 'Preto', 12, 3, 10 UNION ALL
                  SELECT 24, 'PUM-TEE-BLK-G', 'G', 'Preto', 0, 3, 10 UNION ALL
                  SELECT 25, 'PUM-TEE-BLK-GG', 'GG', 'Preto', 6, 2, 10
              ) AS seed
WHERE NOT EXISTS (SELECT 1 FROM variacao WHERE variacao.id = seed.id);

INSERT INTO cliente (id, nome, cpf, email, telefone, instagram)
SELECT * FROM (
                  SELECT 1 AS id, 'Marcus Vinicius Silva' AS nome, '12345678901' AS cpf, 'marcus.silva@streetwear.com' AS email, '(11) 98765-4321' AS telefone, '@marcus_sneakers' AS instagram UNION ALL
                  SELECT 2, 'Beatriz Santos Ramos', '23456789012', 'bia.ramos@streetwear.com', '(21) 99876-5432', '@biaramos.drip' UNION ALL
                  SELECT 3, 'Thiago Alcantara Dias', '34567890123', 'thiago.dias@streetwear.com', '(31) 97654-3210', '@thiago_kicks' UNION ALL
                  SELECT 4, 'Camila Rocha Nogueira', '45678901234', 'camila.rocha@streetwear.com', '(41) 96543-2109', '@camilarocha_ny'
              ) AS seed
WHERE NOT EXISTS (SELECT 1 FROM cliente WHERE cliente.id = seed.id);

INSERT INTO venda (id, cliente_id, data_venda, forma_pagamento, status, valor_total)
SELECT * FROM (
                  SELECT 1 AS id, 1 AS cliente_id, '2026-11-20 14:30:00' AS data_venda, 'PIX' AS forma_pagamento, 'ENTREGUE' AS status, 2299.80 AS valor_total UNION ALL
                  SELECT 2, 2, '2026-11-21 16:15:00', 'CARTAO_CREDITO', 'PAGA', 699.90 UNION ALL
                  SELECT 3, 3, '2026-11-22 18:45:00', 'PIX', 'PENDENTE', 1049.80
              ) AS seed
WHERE NOT EXISTS (SELECT 1 FROM venda WHERE venda.id = seed.id);

INSERT INTO item_venda (id, venda_id, variacao_id, quantidade, preco_unitario)
SELECT * FROM (
                  SELECT 1 AS id, 1 AS venda_id, 1 AS variacao_id, 1 AS quantidade, 799.90 AS preco_unitario UNION ALL
                  SELECT 2, 1, 8, 1, 1499.90 UNION ALL
                  SELECT 3, 2, 12, 1, 699.90 UNION ALL
                  SELECT 4, 3, 2, 1, 799.90 UNION ALL
                  SELECT 5, 3, 21, 1, 249.90
              ) AS seed
WHERE NOT EXISTS (SELECT 1 FROM item_venda WHERE item_venda.id = seed.id);