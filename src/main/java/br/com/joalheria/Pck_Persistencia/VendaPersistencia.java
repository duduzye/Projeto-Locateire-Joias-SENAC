package br.com.joalheria.Pck_Persistencia;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Repository
public class VendaPersistencia {

    private final JdbcTemplate jdbc;

    public VendaPersistencia(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // CONSULTAS

    public List<Map<String, Object>> vendas() {
        return jdbc.queryForList("CALL PROC_SEL_VENDAS()");
    }

    public Map<String, Object> nota(int venda) {
        List<Map<String, Object>> r =
                jdbc.queryForList("CALL PROC_SEL_NOTA_FISCAL(?)", venda);

        return r.isEmpty() ? null : r.get(0);
    }

    public List<Map<String, Object>> itensNota(int venda) {
        return jdbc.queryForList("CALL PROC_SEL_ITENS_NOTA(?)", venda);
    }

    // REGISTRO DE VENDA
    // Usados pelo VendaService, dentro da mesma transacao.

    public boolean clienteAtivo(int cliente) {
        Integer total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM CLIENTE_06 WHERE A06_codigo=? AND A06_ativo=TRUE",
                Integer.class,
                cliente
        );

        return total != null && total > 0;
    }

    // Bloqueia a linha do produto (FOR UPDATE) e devolve o saldo atual.
    // Retorna null se o produto nao existir ou estiver inativo.
    public Integer saldoBloqueado(int produto) {
        List<Integer> saldo = jdbc.queryForList(
                "SELECT A05_quantidade_estoque FROM PRODUTO_05 " +
                        "WHERE A05_codigo=? AND A05_ativo=TRUE FOR UPDATE",
                Integer.class,
                produto
        );

        return saldo.isEmpty() ? null : saldo.get(0);
    }

    // LAST_INSERT_ID() vale por conexao, por isso o insert e a leitura do
    // codigo precisam acontecer na mesma transacao.
    @Transactional
    public int inserirVenda(int cliente, int usuario) {
        jdbc.update("CALL PROC_INS_VENDA(?,?)", cliente, usuario);

        Integer venda = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);

        if (venda == null || venda <= 0) {
            throw new IllegalStateException("Não foi possível identificar a venda.");
        }

        return venda;
    }

    // O trigger do banco faz o desconto do estoque e calcula o total da venda.
    public void inserirItem(int venda, int produto, int quantidade) {
        jdbc.update("CALL PROC_INS_ITEM_VENDA(?,?,?)", venda, produto, quantidade);
    }

    // Historico de SAIDA da venda. Nao chamar PROC_SAIDA_ESTOQUE aqui,
    // pois descontaria o estoque duas vezes.
    public void registrarSaidaEstoque(int produto, int usuario, int quantidade, int venda) {
        jdbc.update("""
            INSERT INTO MOVIMENTACAO_ESTOQUE_09(A09_tipo,A09_quantidade,A09_observacao,A05_codigo,A01_codigo)
            VALUES('SAIDA',?,?,?,?)
            """,
                quantidade,
                "Venda #" + venda,
                produto,
                usuario
        );
    }

    public void inserirNotaFiscal(int venda) {
        jdbc.update("CALL PROC_INS_NOTA_FISCAL(?)", venda);
    }
}