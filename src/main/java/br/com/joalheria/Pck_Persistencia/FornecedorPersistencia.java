package br.com.joalheria.Pck_Persistencia;

import br.com.joalheria.Pck_Model.FornecedorForm;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class FornecedorPersistencia {

    private final JdbcTemplate jdbc;

    public FornecedorPersistencia(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // CONSULTAS

    public List<Map<String, Object>> fornecedores() {
        return jdbc.queryForList("CALL PROC_SEL_FORNECEDOR()");
    }

    public List<Map<String, Object>> fornecedoresAtivos() {
        return jdbc.queryForList("""
            SELECT A11_codigo codigo, A11_nome nome
            FROM FORNECEDOR_11
            WHERE A11_ativo=TRUE
            ORDER BY A11_nome
            """);
    }

    public Map<String, Object> fornecedor(int codigo) {
        List<Map<String, Object>> l = jdbc.queryForList("""
            SELECT A11_codigo codigo,
                   A11_nome nome,
                   A11_cnpj cnpj,
                   A11_telefone telefone,
                   A11_email email,
                   A11_endereco endereco,
                   A11_ativo ativo
            FROM FORNECEDOR_11
            WHERE A11_codigo=?
            """, codigo);

        return l.isEmpty() ? null : l.get(0);
    }

    // ALTERACOES

    public void inserirFornecedor(FornecedorForm f) {
        jdbc.update(
                "CALL PROC_INS_FORNECEDOR(?,?,?,?,?)",
                f.nome,
                f.cnpj,
                limpar(f.telefone),
                limpar(f.email),
                limpar(f.endereco)
        );
    }

    public void atualizarFornecedor(int id, FornecedorForm f) {
        jdbc.update(
                "CALL PROC_UPD_FORNECEDOR(?,?,?,?,?,?,?)",
                id,
                f.nome,
                f.cnpj,
                limpar(f.telefone),
                limpar(f.email),
                limpar(f.endereco),
                f.ativo
        );
    }

    public void desativarFornecedor(int id) {
        jdbc.update("CALL PROC_DEL_FORNECEDOR(?)", id);
    }

    // METODO AUXILIAR

    private String limpar(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}