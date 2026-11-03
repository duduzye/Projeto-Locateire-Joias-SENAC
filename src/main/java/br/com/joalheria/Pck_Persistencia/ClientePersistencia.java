package br.com.joalheria.Pck_Persistencia;

import br.com.joalheria.Pck_Model.ClienteForm;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class ClientePersistencia {

    private final JdbcTemplate jdbc;

    public ClientePersistencia(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // CONSULTAS

    public List<Map<String, Object>> clientes() {
        return jdbc.queryForList("CALL PROC_SEL_CLIENTE()");
    }

    public List<Map<String, Object>> clientesAtivos() {
        return jdbc.queryForList("""
            SELECT A06_codigo codigo, A06_nome nome
            FROM CLIENTE_06
            WHERE A06_ativo=TRUE
            ORDER BY A06_nome
            """);
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

    // ALTERACOES

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

    // METODO AUXILIAR

    private String limpar(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}