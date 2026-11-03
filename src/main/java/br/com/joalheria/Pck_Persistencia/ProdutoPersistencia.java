package br.com.joalheria.Pck_Persistencia;

import br.com.joalheria.Pck_Model.ProdutoForm;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class ProdutoPersistencia {

    private final JdbcTemplate jdbc;

    public ProdutoPersistencia(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // CONSULTAS

    public List<Map<String, Object>> produtos() {
        return jdbc.queryForList("CALL PROC_SEL_PRODUTO()");
    }

    public List<Map<String, Object>> produtosAtivos() {
        return jdbc.queryForList("""
            SELECT A05_codigo codigo,
                   A05_nome nome,
                   A05_sku sku,
                   A05_preco preco,
                   A05_quantidade_estoque estoque
            FROM PRODUTO_05
            WHERE A05_ativo=TRUE
            ORDER BY A05_nome
            """);
    }

    public Map<String, Object> produto(int codigo) {
        List<Map<String, Object>> l = jdbc.queryForList("""
            SELECT A05_codigo codigo,
                   A05_sku sku,
                   A05_nome nome,
                   A05_descricao descricao,
                   A05_preco preco,
                   A05_tamanho tamanho,
                   A05_peso_gramas peso,
                   A05_ativo ativo,
                   A02_codigo categoria,
                   A03_codigo material,
                   A04_codigo pedra
            FROM PRODUTO_05
            WHERE A05_codigo=?
            """, codigo);

        return l.isEmpty() ? null : l.get(0);
    }

    public List<Map<String, Object>> categorias() {
        return jdbc.queryForList(
                "SELECT A02_codigo codigo, A02_nome nome " +
                        "FROM CATEGORIA_02 WHERE A02_ativo=TRUE ORDER BY A02_nome"
        );
    }

    public List<Map<String, Object>> materiais() {
        return jdbc.queryForList(
                "SELECT A03_codigo codigo, A03_nome nome " +
                        "FROM MATERIAL_03 WHERE A03_ativo=TRUE ORDER BY A03_nome"
        );
    }

    public List<Map<String, Object>> pedras() {
        return jdbc.queryForList(
                "SELECT A04_codigo codigo, A04_nome nome " +
                        "FROM PEDRA_04 WHERE A04_ativo=TRUE ORDER BY A04_nome"
        );
    }

    // ALTERACOES

    public void inserirProduto(ProdutoForm p) {
        jdbc.update(
                "CALL PROC_INS_PRODUTO(?,?,?,?,?,?,?,?,?)",
                p.sku,
                p.nome,
                limpar(p.descricao),
                p.preco,
                limpar(p.tamanho),
                p.peso,
                p.categoria,
                p.material,
                p.pedra
        );
    }

    public void atualizarProduto(int id, ProdutoForm p) {
        jdbc.update(
                "CALL PROC_UPD_PRODUTO(?,?,?,?,?,?,?,?,?,?,?)",
                id,
                p.sku,
                p.nome,
                limpar(p.descricao),
                p.preco,
                limpar(p.tamanho),
                p.peso,
                p.categoria,
                p.material,
                p.pedra,
                p.ativo
        );
    }

    public void desativarProduto(int id) {
        jdbc.update("CALL PROC_DEL_PRODUTO(?)", id);
    }

    // METODO AUXILIAR

    private String limpar(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}