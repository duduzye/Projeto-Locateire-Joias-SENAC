package br.com.joalheria.Pck_Persistencia;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public class RelatorioPersistencia {

    private final JdbcTemplate jdbc;

    public RelatorioPersistencia(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
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
}