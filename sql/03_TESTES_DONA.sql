-- Testes demonstrativos, para banco novo (01) ou migrado (02).
-- Rode a parte de INSERCOES apenas UMA VEZ para nao duplicar SKU / CPF.
-- Para verificar tabelas sem alterar registros, use somente os SELECT e CALL de consulta.
USE BD_JOALHERIA;

-- LOGIN - o segundo nao deve retornar usuarios
CALL PROC_LOGIN_USUARIO('dona','123456');
CALL PROC_LOGIN_USUARIO('dona','senhaerrada');

-- Cria exemplo de produto sem quantidade inicial
CALL PROC_INS_PRODUTO('ANL-DEMO-001','Anel Aurora Demonstração','Ouro 18k com diamante',1990.00,'16',3.50,1,1,2);
CALL PROC_BUSCA_PRODUTO('ANL-DEMO-001');

-- Sem depender de ID 1: pega o codigo real da dona e do produto.
SET @usuario_dona=(SELECT A01_codigo FROM USUARIO_01 WHERE A01_login='dona');
SET @produto_demo=(SELECT A05_codigo FROM PRODUTO_05 WHERE A05_sku='ANL-DEMO-001');
CALL PROC_ENTRADA_ESTOQUE(@produto_demo,@usuario_dona,5,'Entrada inicial de demonstração');
CALL PROC_BUSCA_PRODUTO('ANL-DEMO-001');

-- Cria cliente de teste
CALL PROC_INS_CLIENTE('Mariana Souza Silva','12345678901','11988887777','mariana.silva@email.com');
CALL PROC_SEL_CLIENTE();

-- Abre venda e guarda o codigo real gerado na conexao atual
SET @cliente_demo=(SELECT A06_codigo FROM CLIENTE_06 WHERE A06_cpf='12345678901');
CALL PROC_INS_VENDA(@cliente_demo,@usuario_dona);
SET @venda_demo=LAST_INSERT_ID();
CALL PROC_INS_ITEM_VENDA(@venda_demo,@produto_demo,2);
CALL PROC_INS_ITEM_VENDA(@venda_demo,@produto_demo,1);
CALL PROC_INS_NOTA_FISCAL(@venda_demo);
CALL PROC_SEL_VENDAS();
CALL PROC_SEL_NOTA_FISCAL(@venda_demo);
CALL PROC_SEL_ITENS_NOTA(@venda_demo);
SELECT * FROM PRODUTO_05 WHERE A05_codigo=@produto_demo;

-- Dashboard usando periodo que inclui o momento atual (nao agosto fixo)
SET @inicio=DATE_FORMAT(CURDATE(),'%Y-%m-01 00:00:00');
SET @fim=CONCAT(LAST_DAY(CURDATE()),' 23:59:59');
SELECT FN_TOTAL_VENDAS(@inicio,@fim) AS total_vendido;
SELECT FN_TICKET_MEDIO(@inicio,@fim) AS ticket_medio;
CALL PROC_DASHBOARD(@inicio,@fim);
CALL PROC_DASHBOARD_DETALHADO(@inicio,@fim);
