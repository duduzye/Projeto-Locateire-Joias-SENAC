
package br.com.joalheria.repository;

import br.com.joalheria.model.ClienteForm;
import br.com.joalheria.model.ProdutoForm;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public class JoalheriaRepository {

    private final JdbcTemplate jdbc;

    public JoalheriaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // CONSULTAS GERAIS

    public List<Map<String, Object>> produtos() {
        return jdbc.queryForList("CALL PROC_SEL_PRODUTO()");
    }

    public List<Map<String, Object>> clientes() {
        return jdbc.queryForList("CALL PROC_SEL_CLIENTE()");
    }

    public List<Map<String, Object>> vendas() {
        return jdbc.queryForList("CALL PROC_SEL_VENDAS()");
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

    public List<Map<String, Object>> clientesAtivos() {
        return jdbc.queryForList("""
            SELECT A06_codigo codigo, A06_nome nome
            FROM CLIENTE_06
            WHERE A06_ativo=TRUE
            ORDER BY A06_nome
            """);
    }

    public List<Map<String, Object>> movimentacoes() {
        return jdbc.queryForList("""
            SELECT M.A09_data data,
                   M.A09_tipo tipo,
                   M.A09_quantidade quantidade,
                   M.A09_observacao observacao,
                   P.A05_nome produto,
                   U.A01_nome responsavel
            FROM MOVIMENTACAO_ESTOQUE_09 M
            JOIN PRODUTO_05 P ON P.A05_codigo=M.A05_codigo
            JOIN USUARIO_01 U ON U.A01_codigo=M.A01_codigo
            ORDER BY M.A09_data DESC, M.A09_codigo DESC
            LIMIT 100
            """);
    }

    // CONSULTAS INDIVIDUAIS

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

    public Map<String, Object> cliente(int codigo) {
        List<Map<String, Object>> l = jdbc.queryForList("""
            SELECT A06_codigo codigo,
                   A06_nome nome,
                   A06_cpf cpf,
                   A06_telefone telefone,
                   A06_email email,
                   A06_ativo ativo
            FROM CLIENTE_06
            WHERE A06_codigo=?
            """, codigo);

        return l.isEmpty() ? null : l.get(0);
    }

    // NOTAS FISCAIS

    public Map<String, Object> nota(int venda) {
        List<Map<String, Object>> r =
                jdbc.queryForList("CALL PROC_SEL_NOTA_FISCAL(?)", venda);

        return r.isEmpty() ? null : r.get(0);
    }

    public List<Map<String, Object>> itensNota(int venda) {
        return jdbc.queryForList("CALL PROC_SEL_ITENS_NOTA(?)", venda);
    }

    // DASHBOARD

    public Map<String, Object> dashboard(
            LocalDate inicio, LocalDate fim) {

        return jdbc.queryForMap(
                "CALL PROC_DASHBOARD(?,?)",
                Timestamp.valueOf(inicio.atStartOfDay()),
                Timestamp.valueOf(fim.atTime(23, 59, 59))
        );
    }

    public List<Map<String, Object>> dashboardDetalhado(
            LocalDate inicio, LocalDate fim) {

        return jdbc.queryForList(
                "CALL PROC_DASHBOARD_DETALHADO(?,?)",
                Timestamp.valueOf(inicio.atStartOfDay()),
                Timestamp.valueOf(fim.atTime(23, 59, 59))
        );
    }

    // NOVA FUNCIONALIDADE DO PI:
    // RANKING DOS 10 PRODUTOS MAIS VENDIDOS

    public List<Map<String, Object>> produtosMaisVendidos(
            LocalDate inicio, LocalDate fim) {

        String sql = """
            SELECT
                p.A05_codigo AS codigo,
                p.A05_nome AS produto,
                p.A05_sku AS sku,
                SUM(i.A08_quantidade) AS quantidade_vendida,
                SUM(i.A08_valor_item) AS faturamento_total
            FROM ITEM_VENDA_08 i
            INNER JOIN PRODUTO_05 p
                ON i.A05_codigo = p.A05_codigo
            INNER JOIN VENDA_07 v
                ON i.A07_codigo = v.A07_codigo
            WHERE v.A07_data >= ?
              AND v.A07_data < ?
            GROUP BY
                p.A05_codigo,
                p.A05_nome,
                p.A05_sku
            ORDER BY
                quantidade_vendida DESC,
                p.A05_nome ASC
            LIMIT 10
            """;

        return jdbc.queryForList(
                sql,
                Timestamp.valueOf(inicio.atStartOfDay()),
                Timestamp.valueOf(fim.plusDays(1).atStartOfDay())
        );
    }


    public List<Map<String, Object>> analiseEstoque(
            LocalDate inicio, LocalDate fim) {

        if (inicio.isAfter(fim)) {
            throw new IllegalArgumentException("Período inválido.");
        }

        long dias = java.time.temporal.ChronoUnit.DAYS
                .between(inicio, fim) + 1;

        String sql = """
        SELECT
            p.A05_codigo AS codigo,
            p.A05_nome AS produto,
            p.A05_sku AS sku,
            p.A05_quantidade_estoque AS estoque_atual,

            COALESCE(v.unidades_vendidas, 0)
                AS unidades_vendidas,

            ROUND(
                COALESCE(v.unidades_vendidas, 0) / ?, 2
            ) AS media_diaria,

            CASE
                WHEN COALESCE(v.unidades_vendidas, 0) = 0
                    THEN NULL
                ELSE ROUND(
                    p.A05_quantidade_estoque * ?
                    / v.unidades_vendidas, 1
                )
            END AS dias_cobertura

        FROM PRODUTO_05 p

        LEFT JOIN (
            SELECT
                i.A05_codigo,
                SUM(i.A08_quantidade) AS unidades_vendidas
            FROM ITEM_VENDA_08 i
            INNER JOIN VENDA_07 venda
                ON i.A07_codigo = venda.A07_codigo
            WHERE venda.A07_data >= ?
              AND venda.A07_data < ?
            GROUP BY i.A05_codigo
        ) v ON p.A05_codigo = v.A05_codigo

        WHERE p.A05_ativo = TRUE

        ORDER BY unidades_vendidas ASC, p.A05_nome ASC
        """;

        return jdbc.queryForList(
                sql,
                dias,
                dias,
                Timestamp.valueOf(inicio.atStartOfDay()),
                Timestamp.valueOf(fim.plusDays(1).atStartOfDay())
        );
    }


    // AUTORIZACAO

    public Integer donaId(String login) {
        List<Integer> ids = jdbc.queryForList(
                "SELECT A01_codigo FROM USUARIO_01 " +
                        "WHERE A01_login=? AND A01_ativo=TRUE " +
                        "AND A01_perfil='DONA'",
                Integer.class,
                login
        );

        if (ids.isEmpty()) {
            throw new IllegalArgumentException("Usuária não autorizada.");
        }

        return ids.get(0);
    }

    // PRODUTOS

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

    // CLIENTES

    public void inserirCliente(ClienteForm c) {
        jdbc.update(
                "CALL PROC_INS_CLIENTE(?,?,?,?)",
                c.nome,
                limpar(c.cpf),
                limpar(c.telefone),
                limpar(c.email)
        );
    }

    public void atualizarCliente(int id, ClienteForm c) {
        jdbc.update(
                "CALL PROC_UPD_CLIENTE(?,?,?,?,?,?)",
                id,
                c.nome,
                limpar(c.cpf),
                limpar(c.telefone),
                limpar(c.email),
                c.ativo
        );
    }

    public void desativarCliente(int id) {
        jdbc.update("CALL PROC_DEL_CLIENTE(?)", id);
    }

    // ESTOQUE

    @Transactional
    public void entradaEstoque(
            int produto, int usuario, int quantidade, String obs) {

        jdbc.update(
                "CALL PROC_ENTRADA_ESTOQUE(?,?,?,?)",
                produto,
                usuario,
                quantidade,
                limpar(obs)
        );
    }

    @Transactional
    public void saidaEstoque(
            int produto, int usuario, int quantidade, String obs) {

        jdbc.update(
                "CALL PROC_SAIDA_ESTOQUE(?,?,?,?)",
                produto,
                usuario,
                quantidade,
                limpar(obs)
        );
    }

    // SENHA

    public void trocarSenha(
            int usuario, String antiga, String nova) {

        jdbc.update(
                "CALL PROC_TROCAR_SENHA_DONA(?,?,?)",
                usuario,
                antiga,
                nova
        );
    }

    // METODO AUXILIAR

    private String limpar(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
