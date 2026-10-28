-- =====================================================================
-- MIGRACAO NO BANCO JA EXISTENTE - NAO TEM DROP DATABASE / DELETE
-- Rode se voce JA executou o script original e possui dados no Workbench.
-- Faca backup antes de alterar procedures. Reexecutavel.
-- Nao remove linhas historicas de gerente/funcionaria ligadas a vendas.
-- Eles serao DESATIVADOS; login e estoque so admitem a dona.
-- =====================================================================
USE BD_JOALHERIA;

UPDATE USUARIO_01 SET A01_ativo=FALSE WHERE A01_perfil<>'DONA';
INSERT INTO USUARIO_01(A01_nome,A01_login,A01_senha,A01_perfil,A01_ativo)
SELECT 'Dona Joalheria','dona',SHA2('123456',256),'DONA',TRUE
WHERE NOT EXISTS(SELECT 1 FROM USUARIO_01 WHERE A01_login='dona');
UPDATE USUARIO_01 SET A01_ativo=TRUE WHERE A01_login='dona' AND A01_perfil='DONA';
-- OBS: Nao muda senha existente da dona para nao causar perda de acesso.
-- O CHECK historico segue permitindo perfis antigos SOMENTE para reter registros.

DROP PROCEDURE IF EXISTS PROC_LOGIN_USUARIO;
DROP PROCEDURE IF EXISTS PROC_ENTRADA_ESTOQUE;
DROP PROCEDURE IF EXISTS PROC_SAIDA_ESTOQUE;
DROP PROCEDURE IF EXISTS PROC_INS_USUARIO;
DROP PROCEDURE IF EXISTS PROC_SEL_USUARIO;
DROP PROCEDURE IF EXISTS PROC_UPD_USUARIO;
DROP PROCEDURE IF EXISTS PROC_DEL_USUARIO;
DROP PROCEDURE IF EXISTS PROC_TROCAR_SENHA_DONA;
DROP PROCEDURE IF EXISTS PROC_INS_VENDA;
DROP PROCEDURE IF EXISTS PROC_INS_NOTA_FISCAL;
DROP PROCEDURE IF EXISTS PROC_INS_FORNECEDOR;
DROP PROCEDURE IF EXISTS PROC_SEL_FORNECEDOR;
DROP PROCEDURE IF EXISTS PROC_BUSCA_FORNECEDOR;
DROP PROCEDURE IF EXISTS PROC_UPD_FORNECEDOR;
DROP PROCEDURE IF EXISTS PROC_DEL_FORNECEDOR;

DELIMITER $$
CREATE PROCEDURE PROC_LOGIN_USUARIO(IN P_login VARCHAR(80), IN P_senha VARCHAR(255))
BEGIN
 SELECT A01_codigo,A01_nome,A01_login,A01_perfil,A01_ativo FROM USUARIO_01
 WHERE A01_login=P_login AND A01_senha=SHA2(P_senha,256) AND A01_perfil='DONA' AND A01_ativo=TRUE;
END $$
CREATE PROCEDURE PROC_ENTRADA_ESTOQUE(IN P_produto INT,IN P_usuario INT,IN P_quantidade INT,IN P_observacao VARCHAR(255),IN P_fornecedor INT)
BEGIN
 IF P_quantidade IS NULL OR P_quantidade<=0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Quantidade deve ser maior que zero'; END IF;
 IF P_fornecedor IS NULL THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Fornecedor obrigatorio para entrada'; END IF;
 IF NOT EXISTS(SELECT 1 FROM USUARIO_01 WHERE A01_codigo=P_usuario AND A01_ativo=TRUE AND A01_perfil='DONA')
 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Somente a dona pode alterar estoque'; END IF;
 IF NOT EXISTS(SELECT 1 FROM FORNECEDOR_11 WHERE A11_codigo=P_fornecedor AND A11_ativo=TRUE)
 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Fornecedor inexistente ou inativo'; END IF;
 UPDATE PRODUTO_05 SET A05_quantidade_estoque=A05_quantidade_estoque+P_quantidade WHERE A05_codigo=P_produto AND A05_ativo=TRUE;
 IF ROW_COUNT()<>1 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Produto inexistente ou inativo'; END IF;
 INSERT INTO MOVIMENTACAO_ESTOQUE_09(A09_tipo,A09_quantidade,A09_observacao,A05_codigo,A01_codigo,A11_codigo)
 VALUES('ENTRADA',P_quantidade,P_observacao,P_produto,P_usuario,P_fornecedor);
END $$
CREATE PROCEDURE PROC_SAIDA_ESTOQUE(IN P_produto INT,IN P_usuario INT,IN P_quantidade INT,IN P_observacao VARCHAR(255))
BEGIN
 IF P_quantidade IS NULL OR P_quantidade<=0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Quantidade deve ser maior que zero'; END IF;
 IF NOT EXISTS(SELECT 1 FROM USUARIO_01 WHERE A01_codigo=P_usuario AND A01_ativo=TRUE AND A01_perfil='DONA')
 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Somente a dona pode alterar estoque'; END IF;
 UPDATE PRODUTO_05 SET A05_quantidade_estoque=A05_quantidade_estoque-P_quantidade
 WHERE A05_codigo=P_produto AND A05_ativo=TRUE AND A05_quantidade_estoque>=P_quantidade;
 IF ROW_COUNT()<>1 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Estoque insuficiente ou produto inativo'; END IF;
 INSERT INTO MOVIMENTACAO_ESTOQUE_09(A09_tipo,A09_quantidade,A09_observacao,A05_codigo,A01_codigo) VALUES('SAIDA',P_quantidade,P_observacao,P_produto,P_usuario);
END $$
CREATE PROCEDURE PROC_INS_USUARIO(IN P_nome VARCHAR(100),IN P_login VARCHAR(80),IN P_senha VARCHAR(255),IN P_perfil VARCHAR(20))
BEGIN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Cadastro de usuarios desabilitado: somente a dona'; END $$
CREATE PROCEDURE PROC_SEL_USUARIO()
BEGIN SELECT A01_codigo,A01_nome,A01_login,A01_perfil,A01_ativo FROM USUARIO_01 WHERE A01_perfil='DONA' ORDER BY A01_nome; END $$
CREATE PROCEDURE PROC_UPD_USUARIO(IN P_codigo INT,IN P_nome VARCHAR(100),IN P_login VARCHAR(80),IN P_perfil VARCHAR(20),IN P_ativo BOOLEAN)
BEGIN
 IF P_perfil<>'DONA' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Perfil nao permitido'; END IF;
 UPDATE USUARIO_01 SET A01_nome=P_nome,A01_login=P_login,A01_ativo=P_ativo WHERE A01_codigo=P_codigo AND A01_perfil='DONA';
END $$
CREATE PROCEDURE PROC_DEL_USUARIO(IN P_codigo INT)
BEGIN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Desativacao da proprietaria nao permitida'; END $$
CREATE PROCEDURE PROC_TROCAR_SENHA_DONA(IN P_codigo INT,IN P_senha_atual VARCHAR(255),IN P_nova_senha VARCHAR(255))
BEGIN
 IF CHAR_LENGTH(P_nova_senha)<8 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Senha nova muito curta'; END IF;
 UPDATE USUARIO_01 SET A01_senha=SHA2(P_nova_senha,256) WHERE A01_codigo=P_codigo AND A01_perfil='DONA' AND A01_ativo=TRUE AND A01_senha=SHA2(P_senha_atual,256);
 IF ROW_COUNT()<>1 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Senha atual incorreta'; END IF;
END $$
CREATE PROCEDURE PROC_INS_VENDA(IN P_cliente INT,IN P_usuario INT)
BEGIN
 IF NOT EXISTS(SELECT 1 FROM USUARIO_01 WHERE A01_codigo=P_usuario AND A01_perfil='DONA' AND A01_ativo=TRUE) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Usuario sem permissao'; END IF;
 IF NOT EXISTS(SELECT 1 FROM CLIENTE_06 WHERE A06_codigo=P_cliente AND A06_ativo=TRUE) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Cliente nao existe ou inativo'; END IF;
 INSERT INTO VENDA_07(A06_codigo,A01_codigo) VALUES(P_cliente,P_usuario);
END $$
CREATE PROCEDURE PROC_INS_NOTA_FISCAL(IN P_venda INT)
BEGIN
 DECLARE V_total DECIMAL(10,2) DEFAULT NULL; DECLARE V_numero VARCHAR(50);
 SELECT A07_valor_total INTO V_total FROM VENDA_07 WHERE A07_codigo=P_venda;
 IF V_total IS NULL OR V_total<=0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Venda inexistente ou sem itens'; END IF;
 SET V_numero=CONCAT('NF-',DATE_FORMAT(NOW(),'%Y%m%d'),'-',LPAD(P_venda,6,'0'));
 INSERT INTO NOTA_FISCAL_10(A10_numero,A10_valor_total,A07_codigo) VALUES(V_numero,V_total,P_venda);
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
BEGIN UPDATE FORNECEDOR_11 SET A11_ativo=FALSE WHERE A11_codigo=P_codigo; END $$
DELIMITER ;

-- Validacoes manuais:
-- CALL PROC_LOGIN_USUARIO('dona','123456'); (se senha nunca foi alterada)
-- SELECT A01_nome,A01_perfil,A01_ativo FROM USUARIO_01;
