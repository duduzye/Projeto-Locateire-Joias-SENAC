package br.com.joalheria.Pck_Persistencia;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public class EstoquePersistencia {

    private final JdbcTemplate jdbc;

    public EstoquePersistencia(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // CONSULTAS

    public List<Map<String, Object>> movimentacoes() {
        return jdbc.queryForList("""
            SELECT M.A09_data data,
                   M.A09_tipo tipo,
                   M.A09_quantidade quantidade,
                   M.A09_observacao observacao,
                   P.A05_nome produto,
                   U.A01_nome responsavel,
                   F.A11_nome fornecedor
            FROM MOVIMENTACAO_ESTOQUE_09 M
            JOIN PRODUTO_05 P ON P.A05_codigo=M.A05_codigo
            JOIN USUARIO_01 U ON U.A01_codigo=M.A01_codigo
            LEFT JOIN FORNECEDOR_11 F ON F.A11_codigo=M.A11_codigo
            ORDER BY M.A09_data DESC, M.A09_codigo DESC
            LIMIT 100
            """);
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

    // MOVIMENTACOES

    @Transactional
    public void entradaEstoque(
            int produto, int usuario, int quantidade, String obs, Integer fornecedor) {

        jdbc.update(
                "CALL PROC_ENTRADA_ESTOQUE(?,?,?,?,?)",
                produto,
                usuario,
                quantidade,
                limpar(obs),
                fornecedor
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

    // METODO AUXILIAR

    private String limpar(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}