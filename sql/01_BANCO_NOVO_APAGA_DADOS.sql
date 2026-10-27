-- =====================================================================
-- JOALHERIA DA DONA - INSTALACAO DO ZERO
-- ATENCAO: ESTE ARQUIVO EXCLUI O BANCO BD_JOALHERIA E SEUS DADOS!
-- Se voce ja tem dados no Workbench, use 02_MIGRACAO_SEM_APAGAR_DADOS.sql
-- MySQL 8.0+ | Executar pelo MySQL Workbench, script inteiro.
-- =====================================================================
DROP DATABASE IF EXISTS BD_JOALHERIA;
CREATE DATABASE BD_JOALHERIA CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE BD_JOALHERIA;

CREATE TABLE USUARIO_01 (
 A01_codigo INT AUTO_INCREMENT PRIMARY KEY,
 A01_nome VARCHAR(100) NOT NULL,
 A01_login VARCHAR(80) NOT NULL UNIQUE,
 A01_senha VARCHAR(255) NOT NULL,
 A01_perfil VARCHAR(20) NOT NULL DEFAULT 'DONA',
 A01_ativo BOOLEAN NOT NULL DEFAULT TRUE,
 CONSTRAINT CHK_A01_PERFIL CHECK (A01_perfil='DONA')
);
CREATE TABLE CATEGORIA_02 (
 A02_codigo INT AUTO_INCREMENT PRIMARY KEY,
 A02_nome VARCHAR(50) NOT NULL UNIQUE,
 A02_ativo BOOLEAN NOT NULL DEFAULT TRUE
);
INSERT INTO CATEGORIA_02 (A02_nome) VALUES ('Anel'),('Aliança'),('Colar'),('Pulseira'),('Brinco'),('Pingente'),('Tornozeleira');
CREATE TABLE MATERIAL_03 (
 A03_codigo INT AUTO_INCREMENT PRIMARY KEY,
 A03_nome VARCHAR(50) NOT NULL UNIQUE,
 A03_ativo BOOLEAN NOT NULL DEFAULT TRUE
);
INSERT INTO MATERIAL_03 (A03_nome) VALUES ('Ouro 18k'),('Ouro 14k'),('Prata 925'),('Aço Inoxidável');
CREATE TABLE PEDRA_04 (
 A04_codigo INT AUTO_INCREMENT PRIMARY KEY,
 A04_nome VARCHAR(50) NOT NULL UNIQUE,
 A04_ativo BOOLEAN NOT NULL DEFAULT TRUE
);
INSERT INTO PEDRA_04 (A04_nome) VALUES ('Sem Pedra'),('Diamante'),('Zircônia'),('Esmeralda'),('Rubi'),('Safira');
CREATE TABLE PRODUTO_05 (
 A05_codigo INT AUTO_INCREMENT PRIMARY KEY,
 A05_sku VARCHAR(30) NOT NULL UNIQUE,
 A05_nome VARCHAR(100) NOT NULL,
 A05_descricao VARCHAR(255),
 A05_preco DECIMAL(10,2) NOT NULL,
 A05_quantidade_estoque INT NOT NULL DEFAULT 0,
 A05_tamanho VARCHAR(20),
 A05_peso_gramas DECIMAL(10,2),
 A05_ativo BOOLEAN NOT NULL DEFAULT TRUE,
 A02_codigo INT NOT NULL,
 A03_codigo INT NOT NULL,
 A04_codigo INT,
 CONSTRAINT CHK_A05_PRECO CHECK(A05_preco > 0),
 CONSTRAINT CHK_A05_ESTOQUE CHECK(A05_quantidade_estoque >= 0),
 CONSTRAINT CHK_A05_PESO CHECK(A05_peso_gramas IS NULL OR A05_peso_gramas > 0),
 CONSTRAINT FK_PRODUTO_CATEGORIA FOREIGN KEY(A02_codigo) REFERENCES CATEGORIA_02(A02_codigo),
 CONSTRAINT FK_PRODUTO_MATERIAL FOREIGN KEY(A03_codigo) REFERENCES MATERIAL_03(A03_codigo),
 CONSTRAINT FK_PRODUTO_PEDRA FOREIGN KEY(A04_codigo) REFERENCES PEDRA_04(A04_codigo)
);
CREATE TABLE CLIENTE_06 (
 A06_codigo INT AUTO_INCREMENT PRIMARY KEY,
 A06_nome VARCHAR(100) NOT NULL,
 A06_cpf VARCHAR(11) UNIQUE,
 A06_telefone VARCHAR(11),
 A06_email VARCHAR(100),
 A06_ativo BOOLEAN NOT NULL DEFAULT TRUE,
 CONSTRAINT CHK_A06_CPF CHECK(A06_cpf IS NULL OR A06_cpf REGEXP '^[0-9]{11}$'),
 CONSTRAINT CHK_A06_TELEFONE CHECK(A06_telefone IS NULL OR A06_telefone REGEXP '^[0-9]{10,11}$')
);
CREATE TABLE VENDA_07 (
 A07_codigo INT AUTO_INCREMENT PRIMARY KEY,
 A07_data DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 A07_valor_total DECIMAL(10,2) NOT NULL DEFAULT 0,
 A06_codigo INT NOT NULL,
 A01_codigo INT NOT NULL,
 CONSTRAINT CHK_A07_TOTAL CHECK(A07_valor_total >= 0),
 CONSTRAINT FK_VENDA_CLIENTE FOREIGN KEY(A06_codigo) REFERENCES CLIENTE_06(A06_codigo),
 CONSTRAINT FK_VENDA_USUARIO FOREIGN KEY(A01_codigo) REFERENCES USUARIO_01(A01_codigo)
);
CREATE TABLE ITEM_VENDA_08 (
 A08_codigo INT NOT NULL,
 A07_codigo INT NOT NULL,
 A05_codigo INT NOT NULL,
 A08_quantidade INT NOT NULL,
 A08_valor_unitario DECIMAL(10,2) NOT NULL,
 A08_valor_item DECIMAL(10,2) NOT NULL,
 PRIMARY KEY(A08_codigo,A07_codigo,A05_codigo),
 CONSTRAINT CHK_A08_QUANTIDADE CHECK(A08_quantidade>0),
 CONSTRAINT CHK_A08_VALOR_UNITARIO CHECK(A08_valor_unitario>0),
 CONSTRAINT CHK_A08_VALOR_ITEM CHECK(A08_valor_item>0),
 CONSTRAINT FK_ITEM_VENDA FOREIGN KEY(A07_codigo) REFERENCES VENDA_07(A07_codigo),
 CONSTRAINT FK_ITEM_PRODUTO FOREIGN KEY(A05_codigo) REFERENCES PRODUTO_05(A05_codigo)
);
CREATE TABLE FORNECEDOR_11 (
 A11_codigo INT AUTO_INCREMENT PRIMARY KEY,
 A11_nome VARCHAR(150) NOT NULL,
 A11_cnpj VARCHAR(14) NOT NULL UNIQUE,
 A11_telefone VARCHAR(11),
 A11_email VARCHAR(100),
 A11_endereco VARCHAR(255),
 A11_ativo BOOLEAN NOT NULL DEFAULT TRUE,
 CONSTRAINT CHK_A11_CNPJ CHECK(A11_cnpj REGEXP '^[0-9]{14}$'),
 CONSTRAINT CHK_A11_TELEFONE CHECK(A11_telefone IS NULL OR A11_telefone REGEXP '^[0-9]{10,11}$')
);
CREATE TABLE MOVIMENTACAO_ESTOQUE_09 (
 A09_codigo INT AUTO_INCREMENT PRIMARY KEY,
 A09_tipo VARCHAR(20) NOT NULL,
 A09_quantidade INT NOT NULL,
 A09_data DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 A09_observacao VARCHAR(255),
 A05_codigo INT NOT NULL,
 A01_codigo INT NOT NULL,
 A11_codigo INT NULL,
 CONSTRAINT CHK_A09_TIPO CHECK(A09_tipo IN ('ENTRADA','SAIDA','AJUSTE')),
 CONSTRAINT CHK_A09_QUANTIDADE CHECK(A09_quantidade>0),
 CONSTRAINT FK_MOV_PRODUTO FOREIGN KEY(A05_codigo) REFERENCES PRODUTO_05(A05_codigo),
 CONSTRAINT FK_MOV_USUARIO FOREIGN KEY(A01_codigo) REFERENCES USUARIO_01(A01_codigo),
 CONSTRAINT FK_MOV_FORNECEDOR FOREIGN KEY(A11_codigo) REFERENCES FORNECEDOR_11(A11_codigo)
);
CREATE TABLE NOTA_FISCAL_10 (
 A10_codigo INT AUTO_INCREMENT PRIMARY KEY,
 A10_numero VARCHAR(50) NOT NULL UNIQUE,
 A10_data_emissao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 A10_valor_total DECIMAL(10,2) NOT NULL,
 A07_codigo INT NOT NULL UNIQUE,
 CONSTRAINT CHK_A10_VALOR CHECK(A10_valor_total>=0),
 CONSTRAINT FK_NOTA_VENDA FOREIGN KEY(A07_codigo) REFERENCES VENDA_07(A07_codigo)
);

-- Conta inicial de demonstração: login dona / senha 123456. ALTERE em Minha Conta.
INSERT INTO USUARIO_01(A01_nome,A01_login,A01_senha,A01_perfil)
VALUES('Dona Joalheria','dona',SHA2('123456',256),'DONA');

DELIMITER $$
CREATE PROCEDURE PROC_INS_USUARIO(IN P_nome VARCHAR(100),IN P_login VARCHAR(80),IN P_senha VARCHAR(255),IN P_perfil VARCHAR(20))
BEGIN
 IF P_perfil <> 'DONA' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Somente a proprietaria pode ser cadastrada'; END IF;
 SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Cadastro de novos usuarios desabilitado: sistema exclusivo da dona';
END $$
CREATE PROCEDURE PROC_SEL_USUARIO()
BEGIN SELECT A01_codigo,A01_nome,A01_login,A01_perfil,A01_ativo FROM USUARIO_01 WHERE A01_perfil='DONA' ORDER BY A01_nome; END $$
CREATE PROCEDURE PROC_UPD_USUARIO(IN P_codigo INT,IN P_nome VARCHAR(100),IN P_login VARCHAR(80),IN P_perfil VARCHAR(20),IN P_ativo BOOLEAN)
BEGIN
 IF P_perfil <> 'DONA' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Perfil nao permitido'; END IF;
 UPDATE USUARIO_01 SET A01_nome=P_nome,A01_login=P_login,A01_perfil='DONA',A01_ativo=P_ativo WHERE A01_codigo=P_codigo AND A01_perfil='DONA';
END $$
CREATE PROCEDURE PROC_DEL_USUARIO(IN P_codigo INT)
BEGIN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Desativacao da unica proprietaria nao permitida'; END $$
CREATE PROCEDURE PROC_LOGIN_USUARIO(IN P_login VARCHAR(80),IN P_senha VARCHAR(255))
BEGIN
 SELECT A01_codigo,A01_nome,A01_login,A01_perfil,A01_ativo FROM USUARIO_01
 WHERE A01_login=P_login AND A01_senha=SHA2(P_senha,256) AND A01_ativo=TRUE AND A01_perfil='DONA';
END $$
CREATE PROCEDURE PROC_TROCAR_SENHA_DONA(IN P_codigo INT,IN P_senha_atual VARCHAR(255),IN P_nova_senha VARCHAR(255))
BEGIN
 IF CHAR_LENGTH(P_nova_senha)<8 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Senha nova muito curta'; END IF;
 UPDATE USUARIO_01 SET A01_senha=SHA2(P_nova_senha,256) WHERE A01_codigo=P_codigo AND A01_perfil='DONA' AND A01_ativo=TRUE AND A01_senha=SHA2(P_senha_atual,256);
 IF ROW_COUNT()<>1 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Senha atual incorreta'; END IF;
END $$

CREATE PROCEDURE PROC_INS_PRODUTO(IN P_sku VARCHAR(30),IN P_nome VARCHAR(100),IN P_descricao VARCHAR(255),IN P_preco DECIMAL(10,2),IN P_tamanho VARCHAR(20),IN P_peso_gramas DECIMAL(10,2),IN P_categoria INT,IN P_material INT,IN P_pedra INT)
BEGIN
 INSERT INTO PRODUTO_05(A05_sku,A05_nome,A05_descricao,A05_preco,A05_quantidade_estoque,A05_tamanho,A05_peso_gramas,A05_ativo,A02_codigo,A03_codigo,A04_codigo)
 VALUES(P_sku,P_nome,P_descricao,P_preco,0,P_tamanho,P_peso_gramas,TRUE,P_categoria,P_material,P_pedra);
END $$
CREATE PROCEDURE PROC_SEL_PRODUTO()
BEGIN
 SELECT P.A05_codigo,P.A05_sku,P.A05_nome,P.A05_descricao,C.A02_nome categoria,M.A03_nome material,PE.A04_nome pedra,P.A05_preco,P.A05_quantidade_estoque,P.A05_tamanho,P.A05_peso_gramas,P.A05_ativo
 FROM PRODUTO_05 P JOIN CATEGORIA_02 C ON P.A02_codigo=C.A02_codigo JOIN MATERIAL_03 M ON P.A03_codigo=M.A03_codigo LEFT JOIN PEDRA_04 PE ON P.A04_codigo=PE.A04_codigo ORDER BY P.A05_nome;
END $$
CREATE PROCEDURE PROC_BUSCA_PRODUTO(IN P_busca VARCHAR(100))
BEGIN
 SELECT P.A05_codigo,P.A05_sku,P.A05_nome,P.A05_descricao,C.A02_nome categoria,M.A03_nome material,PE.A04_nome pedra,P.A05_preco,P.A05_quantidade_estoque,P.A05_tamanho,P.A05_peso_gramas,P.A05_ativo
 FROM PRODUTO_05 P JOIN CATEGORIA_02 C ON P.A02_codigo=C.A02_codigo JOIN MATERIAL_03 M ON P.A03_codigo=M.A03_codigo LEFT JOIN PEDRA_04 PE ON P.A04_codigo=PE.A04_codigo
 WHERE P.A05_ativo=TRUE AND (P.A05_nome LIKE CONCAT('%',P_busca,'%') OR P.A05_sku LIKE CONCAT('%',P_busca,'%')) ORDER BY P.A05_nome;
END $$
CREATE PROCEDURE PROC_UPD_PRODUTO(IN P_codigo INT,IN P_sku VARCHAR(30),IN P_nome VARCHAR(100),IN P_descricao VARCHAR(255),IN P_preco DECIMAL(10,2),IN P_tamanho VARCHAR(20),IN P_peso_gramas DECIMAL(10,2),IN P_categoria INT,IN P_material INT,IN P_pedra INT,IN P_ativo BOOLEAN)
BEGIN
 UPDATE PRODUTO_05 SET A05_sku=P_sku,A05_nome=P_nome,A05_descricao=P_descricao,A05_preco=P_preco,A05_tamanho=P_tamanho,A05_peso_gramas=P_peso_gramas,A02_codigo=P_categoria,A03_codigo=P_material,A04_codigo=P_pedra,A05_ativo=P_ativo WHERE A05_codigo=P_codigo;
END $$
CREATE PROCEDURE PROC_DEL_PRODUTO(IN P_codigo INT)
BEGIN UPDATE PRODUTO_05 SET A05_ativo=FALSE WHERE A05_codigo=P_codigo; END $$

CREATE PROCEDURE PROC_ENTRADA_ESTOQUE(IN P_produto INT,IN P_usuario INT,IN P_quantidade INT,IN P_observacao VARCHAR(255),IN P_fornecedor INT)
BEGIN
 DECLARE V_valido INT DEFAULT 0;
 IF P_quantidade IS NULL OR P_quantidade<=0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='A quantidade deve ser maior que zero'; END IF;
 IF P_fornecedor IS NULL THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Fornecedor obrigatorio para entrada'; END IF;
 SELECT COUNT(*) INTO V_valido FROM USUARIO_01 WHERE A01_codigo=P_usuario AND A01_ativo=TRUE AND A01_perfil='DONA';
 IF V_valido=0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Somente a dona pode alterar estoque'; END IF;
 IF P_fornecedor IS NOT NULL AND NOT EXISTS(SELECT 1 FROM FORNECEDOR_11 WHERE A11_codigo=P_fornecedor AND A11_ativo=TRUE) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Fornecedor inexistente ou inativo';
 END IF;
 UPDATE PRODUTO_05 SET A05_quantidade_estoque=A05_quantidade_estoque+P_quantidade WHERE A05_codigo=P_produto AND A05_ativo=TRUE;
 IF ROW_COUNT()<>1 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Produto inexistente ou inativo'; END IF;
 INSERT INTO MOVIMENTACAO_ESTOQUE_09(A09_tipo,A09_quantidade,A09_observacao,A05_codigo,A01_codigo,A11_codigo)
 VALUES('ENTRADA',P_quantidade,P_observacao,P_produto,P_usuario,P_fornecedor);
END $$
CREATE PROCEDURE PROC_SAIDA_ESTOQUE(IN P_produto INT,IN P_usuario INT,IN P_quantidade INT,IN P_observacao VARCHAR(255))
BEGIN
 DECLARE V_valido INT DEFAULT 0;
 IF P_quantidade IS NULL OR P_quantidade<=0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='A quantidade deve ser maior que zero'; END IF;
 SELECT COUNT(*) INTO V_valido FROM USUARIO_01 WHERE A01_codigo=P_usuario AND A01_ativo=TRUE AND A01_perfil='DONA';
 IF V_valido=0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Somente a dona pode alterar estoque'; END IF;
 UPDATE PRODUTO_05 SET A05_quantidade_estoque=A05_quantidade_estoque-P_quantidade
 WHERE A05_codigo=P_produto AND A05_ativo=TRUE AND A05_quantidade_estoque>=P_quantidade;
 IF ROW_COUNT()<>1 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Estoque insuficiente ou produto inativo'; END IF;
 INSERT INTO MOVIMENTACAO_ESTOQUE_09(A09_tipo,A09_quantidade,A09_observacao,A05_codigo,A01_codigo) VALUES('SAIDA',P_quantidade,P_observacao,P_produto,P_usuario);
END $$

CREATE PROCEDURE PROC_INS_CLIENTE(IN P_nome VARCHAR(100),IN P_cpf VARCHAR(11),IN P_telefone VARCHAR(11),IN P_email VARCHAR(100))
BEGIN INSERT INTO CLIENTE_06(A06_nome,A06_cpf,A06_telefone,A06_email) VALUES(P_nome,P_cpf,P_telefone,P_email); END $$
CREATE PROCEDURE PROC_SEL_CLIENTE()
BEGIN SELECT A06_codigo,A06_nome,A06_cpf,A06_telefone,A06_email,A06_ativo FROM CLIENTE_06 ORDER BY A06_nome; END $$
CREATE PROCEDURE PROC_UPD_CLIENTE(IN P_codigo INT,IN P_nome VARCHAR(100),IN P_cpf VARCHAR(11),IN P_telefone VARCHAR(11),IN P_email VARCHAR(100),IN P_ativo BOOLEAN)
BEGIN UPDATE CLIENTE_06 SET A06_nome=P_nome,A06_cpf=P_cpf,A06_telefone=P_telefone,A06_email=P_email,A06_ativo=P_ativo WHERE A06_codigo=P_codigo; END $$
CREATE PROCEDURE PROC_DEL_CLIENTE(IN P_codigo INT)
BEGIN UPDATE CLIENTE_06 SET A06_ativo=FALSE WHERE A06_codigo=P_codigo; END $$

CREATE PROCEDURE PROC_INS_VENDA(IN P_cliente INT,IN P_usuario INT)
BEGIN
 IF NOT EXISTS(SELECT 1 FROM USUARIO_01 WHERE A01_codigo=P_usuario AND A01_perfil='DONA' AND A01_ativo=TRUE) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Usuario sem permissao'; END IF;
 IF NOT EXISTS(SELECT 1 FROM CLIENTE_06 WHERE A06_codigo=P_cliente AND A06_ativo=TRUE) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Cliente nao existe ou inativo'; END IF;
 INSERT INTO VENDA_07(A06_codigo,A01_codigo) VALUES(P_cliente,P_usuario);
END $$
CREATE PROCEDURE PROC_INS_ITEM_VENDA(IN P_venda INT,IN P_produto INT,IN P_quantidade INT)
BEGIN
 DECLARE V_preco DECIMAL(10,2) DEFAULT NULL;
 DECLARE V_estoque INT DEFAULT NULL;
 DECLARE V_proximo_item INT;
 IF P_quantidade IS NULL OR P_quantidade<=0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Quantidade deve ser maior que zero'; END IF;
 SELECT A05_preco,A05_quantidade_estoque INTO V_preco,V_estoque FROM PRODUTO_05 WHERE A05_codigo=P_produto AND A05_ativo=TRUE;
 IF V_estoque IS NULL THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Produto inexistente ou inativo'; END IF;
 IF P_quantidade>V_estoque THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Estoque insuficiente'; END IF;
 SELECT COALESCE(MAX(A08_codigo),0)+1 INTO V_proximo_item FROM ITEM_VENDA_08 WHERE A07_codigo=P_venda;
 INSERT INTO ITEM_VENDA_08(A08_codigo,A07_codigo,A05_codigo,A08_quantidade,A08_valor_unitario,A08_valor_item)
 VALUES(V_proximo_item,P_venda,P_produto,P_quantidade,V_preco,V_preco*P_quantidade);
END $$
CREATE TRIGGER TRG_ITEM_VENDA_AFTER_INSERT AFTER INSERT ON ITEM_VENDA_08 FOR EACH ROW
BEGIN UPDATE PRODUTO_05 SET A05_quantidade_estoque=A05_quantidade_estoque-NEW.A08_quantidade WHERE A05_codigo=NEW.A05_codigo; END $$
CREATE TRIGGER TRG_ITEM_VENDA_TOTAL_AFTER_INSERT AFTER INSERT ON ITEM_VENDA_08 FOR EACH ROW
BEGIN UPDATE VENDA_07 SET A07_valor_total=(SELECT COALESCE(SUM(A08_valor_item),0) FROM ITEM_VENDA_08 WHERE A07_codigo=NEW.A07_codigo) WHERE A07_codigo=NEW.A07_codigo; END $$
CREATE TRIGGER TRG_ITEM_VENDA_AFTER_DELETE AFTER DELETE ON ITEM_VENDA_08 FOR EACH ROW
BEGIN
 UPDATE PRODUTO_05 SET A05_quantidade_estoque=A05_quantidade_estoque+OLD.A08_quantidade WHERE A05_codigo=OLD.A05_codigo;
 UPDATE VENDA_07 SET A07_valor_total=(SELECT COALESCE(SUM(A08_valor_item),0) FROM ITEM_VENDA_08 WHERE A07_codigo=OLD.A07_codigo) WHERE A07_codigo=OLD.A07_codigo;
END $$
CREATE TRIGGER TRG_ITEM_VENDA_AFTER_UPDATE AFTER UPDATE ON ITEM_VENDA_08 FOR EACH ROW
BEGIN
 UPDATE PRODUTO_05 SET A05_quantidade_estoque=A05_quantidade_estoque+OLD.A08_quantidade-NEW.A08_quantidade WHERE A05_codigo=NEW.A05_codigo;
 UPDATE VENDA_07 SET A07_valor_total=(SELECT COALESCE(SUM(A08_valor_item),0) FROM ITEM_VENDA_08 WHERE A07_codigo=NEW.A07_codigo) WHERE A07_codigo=NEW.A07_codigo;
END $$
CREATE PROCEDURE PROC_DEL_ITEM_VENDA(IN P_venda INT,IN P_item INT)
BEGIN DELETE FROM ITEM_VENDA_08 WHERE A07_codigo=P_venda AND A08_codigo=P_item; END $$
CREATE PROCEDURE PROC_UPD_ITEM_VENDA(IN P_venda INT,IN P_item INT,IN P_quantidade INT)
BEGIN
 DECLARE V_produto INT DEFAULT NULL; DECLARE V_preco DECIMAL(10,2); DECLARE V_quantidade_atual INT; DECLARE V_estoque INT DEFAULT NULL;
 IF P_quantidade IS NULL OR P_quantidade<=0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Quantidade deve ser maior que zero'; END IF;
 SELECT A05_codigo,A08_valor_unitario,A08_quantidade INTO V_produto,V_preco,V_quantidade_atual FROM ITEM_VENDA_08 WHERE A07_codigo=P_venda AND A08_codigo=P_item;
 IF V_produto IS NULL THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Item nao encontrado'; END IF;
 SELECT A05_quantidade_estoque INTO V_estoque FROM PRODUTO_05 WHERE A05_codigo=V_produto;
 IF P_quantidade-V_quantidade_atual>V_estoque THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Estoque insuficiente para alterar a quantidade'; END IF;
 UPDATE ITEM_VENDA_08 SET A08_quantidade=P_quantidade,A08_valor_item=V_preco*P_quantidade WHERE A07_codigo=P_venda AND A08_codigo=P_item;
END $$

CREATE PROCEDURE PROC_INS_NOTA_FISCAL(IN P_venda INT)
BEGIN
 DECLARE V_total DECIMAL(10,2) DEFAULT NULL; DECLARE V_numero VARCHAR(50);
 SELECT A07_valor_total INTO V_total FROM VENDA_07 WHERE A07_codigo=P_venda;
 IF V_total IS NULL OR V_total<=0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Venda inexistente ou sem itens'; END IF;
 SET V_numero=CONCAT('NF-',DATE_FORMAT(NOW(),'%Y%m%d'),'-',LPAD(P_venda,6,'0'));
 INSERT INTO NOTA_FISCAL_10(A10_numero,A10_valor_total,A07_codigo) VALUES(V_numero,V_total,P_venda);
END $$
CREATE PROCEDURE PROC_SEL_NOTA_FISCAL(IN P_venda INT)
BEGIN
 SELECT NF.A10_numero,NF.A10_data_emissao,NF.A10_valor_total,V.A07_codigo venda_codigo,V.A07_data venda_data,
 C.A06_nome cliente_nome,C.A06_cpf cliente_cpf,C.A06_telefone cliente_telefone,C.A06_email cliente_email,U.A01_nome usuario_responsavel
 FROM NOTA_FISCAL_10 NF JOIN VENDA_07 V ON NF.A07_codigo=V.A07_codigo
 JOIN CLIENTE_06 C ON V.A06_codigo=C.A06_codigo JOIN USUARIO_01 U ON V.A01_codigo=U.A01_codigo WHERE NF.A07_codigo=P_venda;
END $$
CREATE PROCEDURE PROC_SEL_ITENS_NOTA(IN P_venda INT)
BEGIN
 SELECT I.A08_codigo item_codigo,P.A05_sku,P.A05_nome produto_nome,C.A02_nome categoria,M.A03_nome material,PE.A04_nome pedra,I.A08_quantidade,I.A08_valor_unitario,I.A08_valor_item
 FROM ITEM_VENDA_08 I JOIN PRODUTO_05 P ON I.A05_codigo=P.A05_codigo JOIN CATEGORIA_02 C ON P.A02_codigo=C.A02_codigo
 JOIN MATERIAL_03 M ON P.A03_codigo=M.A03_codigo LEFT JOIN PEDRA_04 PE ON P.A04_codigo=PE.A04_codigo
 WHERE I.A07_codigo=P_venda ORDER BY I.A08_codigo;
END $$
CREATE PROCEDURE PROC_SEL_VENDAS()
BEGIN
 SELECT V.A07_codigo,V.A07_data,V.A07_valor_total,C.A06_nome cliente_nome,U.A01_nome usuario_responsavel,U.A01_perfil perfil_responsavel
 FROM VENDA_07 V JOIN CLIENTE_06 C ON V.A06_codigo=C.A06_codigo JOIN USUARIO_01 U ON V.A01_codigo=U.A01_codigo ORDER BY V.A07_data DESC;
END $$

CREATE FUNCTION FN_TOTAL_VENDAS(P_data_inicio DATETIME,P_data_fim DATETIME) RETURNS DECIMAL(12,2) READS SQL DATA
BEGIN DECLARE V_total DECIMAL(12,2); SELECT COALESCE(SUM(A07_valor_total),0) INTO V_total FROM VENDA_07 WHERE A07_data BETWEEN P_data_inicio AND P_data_fim; RETURN V_total; END $$
CREATE FUNCTION FN_TICKET_MEDIO(P_data_inicio DATETIME,P_data_fim DATETIME) RETURNS DECIMAL(12,2) READS SQL DATA
BEGIN DECLARE V_media DECIMAL(12,2); SELECT COALESCE(AVG(A07_valor_total),0) INTO V_media FROM VENDA_07 WHERE A07_data BETWEEN P_data_inicio AND P_data_fim; RETURN V_media; END $$
CREATE PROCEDURE PROC_DASHBOARD(IN P_data_inicio DATETIME,IN P_data_fim DATETIME)
BEGIN
 SELECT COUNT(*) quantidade_vendas,FN_TOTAL_VENDAS(P_data_inicio,P_data_fim) total_vendido,FN_TICKET_MEDIO(P_data_inicio,P_data_fim) ticket_medio,
 COALESCE((SELECT SUM(I.A08_quantidade) FROM ITEM_VENDA_08 I JOIN VENDA_07 V ON I.A07_codigo=V.A07_codigo WHERE V.A07_data BETWEEN P_data_inicio AND P_data_fim),0) quantidade_itens_vendidos
 FROM VENDA_07 WHERE A07_data BETWEEN P_data_inicio AND P_data_fim;
END $$
CREATE PROCEDURE PROC_DASHBOARD_DETALHADO(IN P_data_inicio DATETIME,IN P_data_fim DATETIME)
BEGIN
 SELECT V.A07_codigo venda_codigo,V.A07_data data_venda,C.A06_nome cliente,U.A01_nome responsavel,V.A07_valor_total valor_total
 FROM VENDA_07 V JOIN CLIENTE_06 C ON V.A06_codigo=C.A06_codigo JOIN USUARIO_01 U ON V.A01_codigo=U.A01_codigo
 WHERE V.A07_data BETWEEN P_data_inicio AND P_data_fim ORDER BY V.A07_data DESC;
END $$
CREATE PROCEDURE PROC_INS_FORNECEDOR(IN P_nome VARCHAR(150),IN P_cnpj VARCHAR(14),IN P_telefone VARCHAR(11),IN P_email VARCHAR(100),IN P_endereco VARCHAR(255))
BEGIN
 INSERT INTO FORNECEDOR_11(A11_nome,A11_cnpj,A11_telefone,A11_email,A11_endereco)
 VALUES(P_nome,P_cnpj,P_telefone,P_email,P_endereco);
END $$
CREATE PROCEDURE PROC_SEL_FORNECEDOR()
BEGIN
 SELECT A11_codigo,A11_nome,A11_cnpj,A11_telefone,A11_email,A11_endereco,A11_ativo
 FROM FORNECEDOR_11 ORDER BY A11_nome;
END $$
CREATE PROCEDURE PROC_BUSCA_FORNECEDOR(IN P_busca VARCHAR(100))
BEGIN
 SELECT A11_codigo,A11_nome,A11_cnpj,A11_telefone,A11_email,A11_endereco,A11_ativo
 FROM FORNECEDOR_11
 WHERE A11_ativo=TRUE AND (A11_nome LIKE CONCAT('%',P_busca,'%') OR A11_cnpj LIKE CONCAT('%',P_busca,'%'))
 ORDER BY A11_nome;
END $$
CREATE PROCEDURE PROC_UPD_FORNECEDOR(IN P_codigo INT,IN P_nome VARCHAR(150),IN P_cnpj VARCHAR(14),IN P_telefone VARCHAR(11),IN P_email VARCHAR(100),IN P_endereco VARCHAR(255),IN P_ativo BOOLEAN)
BEGIN
 UPDATE FORNECEDOR_11 SET A11_nome=P_nome,A11_cnpj=P_cnpj,A11_telefone=P_telefone,A11_email=P_email,A11_endereco=P_endereco,A11_ativo=P_ativo
 WHERE A11_codigo=P_codigo;
END $$
CREATE PROCEDURE PROC_DEL_FORNECEDOR(IN P_codigo INT)
BEGIN UPDATE FORNECEDOR_11 SET A11_ativo=FALSE WHERE A11_codigo=P_codigo; 
END $$
DELIMITER ;

